package tn.entreprise.escproject.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.entreprise.escproject.dto.ApplicationRequest;
import tn.entreprise.escproject.dto.ApplicationResponse;
import tn.entreprise.escproject.entite.*;
import tn.entreprise.escproject.exception.BadRequestException;
import tn.entreprise.escproject.exception.ConflictException;
import tn.entreprise.escproject.exception.ResourceNotFoundException;
import tn.entreprise.escproject.exception.UnauthorizedException;
import tn.entreprise.escproject.repositories.ApplicationRepository;
import tn.entreprise.escproject.repositories.JobOfferRepository;
import tn.entreprise.escproject.repositories.UserRepository;
import tn.entreprise.escproject.services.Interfaces.IApplicationService;
import tn.entreprise.escproject.services.Interfaces.IService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ApplicationServiceImp implements IService<Application>, IApplicationService {

    private static final Logger log = LoggerFactory.getLogger(ApplicationServiceImp.class);

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private JobOfferRepository jobOfferRepository;

    @Autowired
    private UserRepository userRepository;

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

        ApplicationStatus newStatus;
        try {
            newStatus = ApplicationStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid status: " + status);
        }

        application.setStatus(newStatus);
        applicationRepository.save(application);
        log.info("Application {} status updated to {} by recruiter {}", applicationId, status, recruiterId);

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
                .jobOfferId(app.getJobOffer().getId())
                .jobTitle(app.getJobOffer().getTitle())
                .jobCompany(app.getJobOffer().getCompany())
                .build();
    }
}
