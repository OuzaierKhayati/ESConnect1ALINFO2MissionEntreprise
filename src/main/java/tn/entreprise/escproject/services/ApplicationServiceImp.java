package tn.entreprise.escproject.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import tn.entreprise.escproject.dto.ApplicationRequest;
import tn.entreprise.escproject.dto.ApplicationResponse;
import tn.entreprise.escproject.entite.Application;
import tn.entreprise.escproject.entite.ApplicationStatus;
import tn.entreprise.escproject.entite.JobOffer;
import tn.entreprise.escproject.entite.JobStatus;
import tn.entreprise.escproject.entite.RoleUser;
import tn.entreprise.escproject.entite.User;
import tn.entreprise.escproject.exception.BadRequestException;
import tn.entreprise.escproject.exception.ConflictException;
import tn.entreprise.escproject.exception.ResourceNotFoundException;
import tn.entreprise.escproject.exception.UnauthorizedException;
import tn.entreprise.escproject.repositories.ApplicationRepository;
import tn.entreprise.escproject.repositories.JobOfferRepository;
import tn.entreprise.escproject.repositories.UserProfileRepository;
import tn.entreprise.escproject.repositories.UserRepository;
import tn.entreprise.escproject.services.Interfaces.IApplicationService;
import tn.entreprise.escproject.services.Interfaces.IService;

@Service
public class ApplicationServiceImp implements IService<Application>, IApplicationService {

    private static final Logger log = LoggerFactory.getLogger(ApplicationServiceImp.class);

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private JobOfferRepository jobOfferRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Lazy
    @Autowired
    private NotificationServiceImp notificationService;

    @Autowired
    private EmailService emailService;

    @Value("${app.frontend-url:http://localhost:4200}")
    private String frontendUrl;

    // ======== IService CRUD (kept for backward compat) ========

    @Override
    public Application add(Application application) { return applicationRepository.save(application); }

    @Override
    public Application update(Application application) { return applicationRepository.save(application); }

    @Override
    public void delete(Long id) { applicationRepository.deleteById(id); }

    @Override
    public Application getById(Long id) { return applicationRepository.findById(id).orElse(null); }

    @Override
    public List<Application> getAll() { return (List<Application>) applicationRepository.findAll(); }

    @Override
    public List<Application> addAll(List<Application> list) { return (List<Application>) applicationRepository.saveAll(list); }

    // ======== Business Methods ========

    public ApplicationResponse applyToJob(Long jobId, ApplicationRequest request, Long studentId) {
        // ── Service-layer validation (defence-in-depth) ──────────────────────
        if (request.getCoverLetter() == null || request.getCoverLetter().isBlank()) {
            throw new BadRequestException("Cover letter is required");
        }
        if (request.getCoverLetter().strip().length() < 100) {
            throw new BadRequestException("Cover letter must contain at least 100 characters");
        }
        if (request.getCvUrl() == null || request.getCvUrl().isBlank()) {
            throw new BadRequestException("CV URL is required");
        }
        if (!request.getCvUrl().matches("^https?://.*")) {
            throw new BadRequestException("Please provide a valid CV URL starting with https:// or http://");
        }
        // ────────────────────────────────────────────────────────────────────

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        if (student.getRoleUser() != RoleUser.STUDENT) {
            throw new UnauthorizedException("Only students can apply to jobs");
        }

        JobOffer job = jobOfferRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job offer not found with id: " + jobId));

        if (job.getStatus() != JobStatus.OPEN) {
            throw new BadRequestException("This job offer is no longer accepting applications");
        }

        if (applicationRepository.existsByStudentAndJobOffer(student, job)) {
            throw new ConflictException("You have already applied to this job offer");
        }

        Application application = new Application();
        application.setStudent(student);
        application.setJobOffer(job);
        application.setCoverLetter(request.getCoverLetter());
        application.setCvUrl(request.getCvUrl());
        application.setStatus(ApplicationStatus.PENDING);
        application.setApplicationDate(LocalDateTime.now());

        applicationRepository.save(application);
        log.info("Student {} applied to job {}", studentId, jobId);

        notificationService.notifyRecruiterNewApplication(job.getRecruiter(), student, job, application);

        return toResponse(application);
    }

    public List<ApplicationResponse> getMyApplications(Long studentId) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        return applicationRepository.findByStudent(student).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<ApplicationResponse> getApplicationsForJob(Long jobId, Long recruiterId) {
        JobOffer job = jobOfferRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job offer not found with id: " + jobId));

        if (!job.getRecruiter().getId().equals(recruiterId)) {
            throw new UnauthorizedException("You can only view applications for your own job offers");
        }

        return applicationRepository.findByJobOffer(job).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public ApplicationResponse updateApplicationStatus(Long applicationId, String status, Long recruiterId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id: " + applicationId));

        if (!application.getJobOffer().getRecruiter().getId().equals(recruiterId)) {
            throw new UnauthorizedException("You can only manage applications for your own job offers");
        }

        // Prevent changing status once a final decision has been made
        if (application.getStatus() == ApplicationStatus.ACCEPTED || application.getStatus() == ApplicationStatus.REJECTED) {
            throw new ConflictException("This application already has a final decision (" + application.getStatus() + ") and cannot be modified.");
        }

        ApplicationStatus newStatus;
        try {
            newStatus = ApplicationStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid status: " + status);
        }

        if (newStatus == ApplicationStatus.PENDING) {
            throw new BadRequestException("Cannot set status back to PENDING");
        }

        application.setStatus(newStatus);
        applicationRepository.save(application);
        log.info("Application {} status updated to {} by recruiter {}", applicationId, status, recruiterId);

        notificationService.notifyStudentStatusChanged(application.getStudent(), application.getJobOffer(), application);

        // Send acceptance email asynchronously — do not block the HTTP response
        if (newStatus == ApplicationStatus.ACCEPTED) {
            final Application finalApp = application;
            CompletableFuture.runAsync(() -> sendAcceptanceEmail(finalApp));
        }

        return toResponse(application);
    }

    public void withdrawApplication(Long applicationId, Long studentId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id: " + applicationId));

        if (!application.getStudent().getId().equals(studentId)) {
            throw new UnauthorizedException("You can only withdraw your own applications");
        }

        applicationRepository.delete(application);
        log.info("Student {} withdrew application {}", studentId, applicationId);
    }

    // ======== Helpers ========

    private void sendAcceptanceEmail(Application application) {
        try {
            User student = application.getStudent();
            JobOffer job = application.getJobOffer();
            String jobLink = frontendUrl + "/jobs?highlight=" + job.getId();
            String subject = "Congratulations! Your application for \"" + job.getTitle() + "\" has been accepted";
            String html = buildAcceptanceEmailTemplate(
                    student.getFirstName(),
                    job.getTitle(),
                    job.getCompany(),
                    job.getRecruiter().getFirstName() + " " + job.getRecruiter().getLastName(),
                    jobLink
            );
            emailService.sendHtmlEmail(student.getEmail(), subject, html);
        } catch (Exception e) {
            log.error("Failed to send acceptance email for application {}: {}", application.getId(), e.getMessage());
        }
    }

    private String buildAcceptanceEmailTemplate(String studentName, String jobTitle, String company, String recruiterName, String jobLink) {
        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Application Accepted</title>
            </head>
            <body style="margin: 0; padding: 0; background-color: #f8f9fc; font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;">
                <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="background-color: #f8f9fc; padding: 40px 20px;">
                    <tr>
                        <td align="center">
                            <table role="presentation" width="600" cellpadding="0" cellspacing="0" style="background-color: #ffffff; border-radius: 12px; box-shadow: 0 4px 6px rgba(0,0,0,0.07); overflow: hidden;">
                                <!-- Header -->
                                <tr>
                                    <td style="background: linear-gradient(135deg, #10b981, #059669); padding: 32px 40px; text-align: center;">
                                        <h1 style="margin: 0; color: #ffffff; font-size: 24px; font-weight: 700;">🎉 Application Accepted!</h1>
                                    </td>
                                </tr>
                                <!-- Body -->
                                <tr>
                                    <td style="padding: 40px;">
                                        <p style="font-size: 16px; color: #1e293b; margin: 0 0 16px;">Hello <strong>%s</strong>,</p>
                                        <p style="font-size: 16px; color: #475569; margin: 0 0 24px; line-height: 1.6;">
                                            Great news! Your application for the position <strong style="color: #1e293b;">"%s"</strong>
                                            at <strong style="color: #1e293b;">%s</strong> has been <span style="color: #10b981; font-weight: 700;">accepted</span>.
                                        </p>
                                        <table role="presentation" style="background-color: #f0fdf4; border-radius: 8px; padding: 20px; width: 100%%; margin-bottom: 24px;" cellpadding="0" cellspacing="0">
                                            <tr>
                                                <td style="padding: 16px;">
                                                    <p style="margin: 0 0 8px; font-size: 14px; color: #6b7280;">Position</p>
                                                    <p style="margin: 0 0 16px; font-size: 16px; color: #1e293b; font-weight: 600;">%s</p>
                                                    <p style="margin: 0 0 8px; font-size: 14px; color: #6b7280;">Company</p>
                                                    <p style="margin: 0 0 16px; font-size: 16px; color: #1e293b; font-weight: 600;">%s</p>
                                                    <p style="margin: 0 0 8px; font-size: 14px; color: #6b7280;">Recruiter</p>
                                                    <p style="margin: 0; font-size: 16px; color: #1e293b; font-weight: 600;">%s</p>
                                                </td>
                                            </tr>
                                        </table>
                                        <p style="font-size: 15px; color: #475569; margin: 0 0 28px; line-height: 1.6;">
                                            The recruiter may contact you soon for next steps. In the meantime, you can view the job details below.
                                        </p>
                                        <table role="presentation" cellpadding="0" cellspacing="0" style="margin: 0 auto;">
                                            <tr>
                                                <td style="border-radius: 8px; background: linear-gradient(135deg, #10b981, #059669);">
                                                    <a href="%s" style="display: inline-block; padding: 14px 32px; color: #ffffff; text-decoration: none; font-size: 15px; font-weight: 600;">View Job</a>
                                                </td>
                                            </tr>
                                        </table>
                                    </td>
                                </tr>
                                <!-- Footer -->
                                <tr>
                                    <td style="padding: 24px 40px; background-color: #f8fafc; text-align: center; border-top: 1px solid #e2e8f0;">
                                        <p style="margin: 0; font-size: 13px; color: #94a3b8;">ESConnect &mdash; Connecting Students with Opportunities</p>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                </table>
            </body>
            </html>
            """.formatted(studentName, jobTitle, company, jobTitle, company, recruiterName, jobLink);
    }

    private ApplicationResponse toResponse(Application app) {
        return ApplicationResponse.builder()
                .id(app.getId())
                .applicationDate(app.getApplicationDate())
                .coverLetter(app.getCoverLetter())
                .cvUrl(app.getCvUrl())
                .status(app.getStatus() != null ? app.getStatus().toString() : null)
                .studentId(app.getStudent().getId())
                .studentName(app.getStudent().getFirstName() + " " + app.getStudent().getLastName())
                .studentEmail(app.getStudent().getEmail())
                .studentRole(app.getStudent().getRoleUser() != null ? app.getStudent().getRoleUser().toString() : null)
                .studentProfilePictureUrl(userProfileRepository.findByUserId(app.getStudent().getId())
                    .map(profile -> profile.getProfilePictureUrl())
                    .orElse(null))
                .jobOfferId(app.getJobOffer().getId())
                .jobTitle(app.getJobOffer().getTitle())
                .jobCompany(app.getJobOffer().getCompany())
                .build();
    }
}
