package tn.entreprise.escproject.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.entreprise.escproject.dto.*;
import tn.entreprise.escproject.entite.*;
import tn.entreprise.escproject.exception.BadRequestException;
import tn.entreprise.escproject.exception.ResourceNotFoundException;
import tn.entreprise.escproject.exception.UnauthorizedException;
import tn.entreprise.escproject.repositories.JobCommentRepository;
import tn.entreprise.escproject.repositories.JobOfferRepository;
import tn.entreprise.escproject.repositories.UserRepository;
import tn.entreprise.escproject.services.Interfaces.IJobOfferService;
import tn.entreprise.escproject.services.Interfaces.IService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class JobOfferServiceImp implements IService<JobOffer>, IJobOfferService {

    private static final Logger log = LoggerFactory.getLogger(JobOfferServiceImp.class);

    @Autowired
    private JobOfferRepository jobOfferRepository;

    @Autowired
    private JobCommentRepository jobCommentRepository;

    @Autowired
    private UserRepository userRepository;

    // ======== IService CRUD (kept for backward compat) ========

    @Override
    public JobOffer add(JobOffer jobOffer) { return jobOfferRepository.save(jobOffer); }

    @Override
    public JobOffer update(JobOffer jobOffer) { return jobOfferRepository.save(jobOffer); }

    @Override
    public void delete(Long id) { jobOfferRepository.deleteById(id); }

    @Override
    public JobOffer getById(Long id) { return jobOfferRepository.findById(id).orElse(null); }

    @Override
    public List<JobOffer> getAll() { return (List<JobOffer>) jobOfferRepository.findAll(); }

    @Override
    public List<JobOffer> addAll(List<JobOffer> list) { return (List<JobOffer>) jobOfferRepository.saveAll(list); }

    // ======== Business Methods ========

    public JobOfferResponse createJob(JobOfferRequest request, Long recruiterId) {
        User recruiter = userRepository.findById(recruiterId)
                .orElseThrow(() -> new ResourceNotFoundException("Recruiter not found"));

        if (recruiter.getRoleUser() != RoleUser.RECRUITER) {
            throw new UnauthorizedException("Only recruiters can create job offers");
        }

        JobType jobType;
        try {
            jobType = JobType.valueOf(request.getJobType());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid job type: " + request.getJobType());
        }

        JobOffer job = new JobOffer();
        job.setTitle(request.getTitle());
        job.setDescription(request.getDescription());
        job.setCompany(request.getCompany());
        job.setLocation(request.getLocation());
        job.setRequirements(request.getRequirements());
        job.setSalary(request.getSalary());
        job.setJobType(jobType);
        job.setStatus(JobStatus.OPEN);
        job.setCreatedAt(LocalDateTime.now());
        job.setRecruiter(recruiter);

        jobOfferRepository.save(job);
        log.info("Job created: '{}' by recruiter {}", job.getTitle(), recruiter.getEmail());
        return toResponse(job, null);
    }

    public JobOfferResponse updateJob(Long jobId, JobOfferRequest request, Long recruiterId) {
        JobOffer job = findJobOrThrow(jobId);

        if (!job.getRecruiter().getId().equals(recruiterId)) {
            throw new UnauthorizedException("You can only edit your own job offers");
        }

        job.setTitle(request.getTitle());
        job.setDescription(request.getDescription());
        job.setCompany(request.getCompany());
        job.setLocation(request.getLocation());
        job.setRequirements(request.getRequirements());
        job.setSalary(request.getSalary());

        if (request.getJobType() != null) {
            try {
                job.setJobType(JobType.valueOf(request.getJobType()));
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Invalid job type: " + request.getJobType());
            }
        }

        jobOfferRepository.save(job);
        log.info("Job updated: id={}", jobId);
        return toResponse(job, null);
    }

    public void deleteJob(Long jobId, Long recruiterId) {
        JobOffer job = findJobOrThrow(jobId);
        if (!job.getRecruiter().getId().equals(recruiterId)) {
            throw new UnauthorizedException("You can only delete your own job offers");
        }
        jobOfferRepository.delete(job);
        log.info("Job deleted: id={}", jobId);
    }

    public JobOfferResponse toggleJobStatus(Long jobId, Long recruiterId) {
        JobOffer job = findJobOrThrow(jobId);
        if (!job.getRecruiter().getId().equals(recruiterId)) {
            throw new UnauthorizedException("You can only manage your own job offers");
        }
        job.setStatus(job.getStatus() == JobStatus.OPEN ? JobStatus.CLOSED : JobStatus.OPEN);
        jobOfferRepository.save(job);
        log.info("Job {} status toggled to {}", jobId, job.getStatus());
        return toResponse(job, recruiterId);
    }

    public List<JobOfferResponse> getAllJobs(Long currentUserId) {
        List<JobOffer> jobs = jobOfferRepository.findAllByOrderByCreatedAtDesc();
        return jobs.stream().map(j -> toResponse(j, currentUserId)).collect(Collectors.toList());
    }

    public List<JobOfferResponse> getMyJobs(Long recruiterId) {
        User recruiter = userRepository.findById(recruiterId)
                .orElseThrow(() -> new ResourceNotFoundException("Recruiter not found"));
        if (recruiter.getRoleUser() != RoleUser.RECRUITER) {
            throw new UnauthorizedException("Only recruiters can access their own job posts");
        }
        List<JobOffer> jobs = jobOfferRepository.findByRecruiterOrderByCreatedAtDesc(recruiter);
        return jobs.stream().map(j -> toResponse(j, recruiterId)).collect(Collectors.toList());
    }

    public JobOfferResponse getJobById(Long jobId, Long currentUserId) {
        JobOffer job = findJobOrThrow(jobId);
        return toResponse(job, currentUserId);
    }

    public List<JobOfferResponse> searchJobs(String query, Long currentUserId) {
        List<JobOffer> jobs = jobOfferRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(query, query);
        return jobs.stream().map(j -> toResponse(j, currentUserId)).collect(Collectors.toList());
    }

    // ======== Like / Unlike ========

    public boolean toggleLike(Long jobId, Long userId) {
        JobOffer job = findJobOrThrow(jobId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        boolean alreadyLiked = job.getLikedBy().stream().anyMatch(u -> u.getId().equals(userId));

        if (alreadyLiked) {
            job.getLikedBy().removeIf(u -> u.getId().equals(userId));
            jobOfferRepository.save(job);
            log.info("User {} unliked job {}", userId, jobId);
            return false;
        } else {
            job.getLikedBy().add(user);
            jobOfferRepository.save(job);
            log.info("User {} liked job {}", userId, jobId);
            return true;
        }
    }

    public List<UserResponse> getJobLikers(Long jobId) {
        JobOffer job = findJobOrThrow(jobId);
        return job.getLikedBy().stream()
                .map(u -> {
                    UserResponse r = new UserResponse();
                    r.setId(u.getId());
                    r.setFirstName(u.getFirstName());
                    r.setLastName(u.getLastName());
                    r.setEmail(u.getEmail());
                    r.setRoleUser(u.getRoleUser() != null ? u.getRoleUser().toString() : null);
                    return r;
                })
                .collect(Collectors.toList());
    }

    // ======== Comments ========

    public CommentResponse addComment(Long jobId, Long userId, String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new BadRequestException("Comment content cannot be empty");
        }

        JobOffer job = findJobOrThrow(jobId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        JobComment comment = new JobComment();
        comment.setContent(content.trim());
        comment.setCreatedAt(LocalDateTime.now());
        comment.setUser(user);
        comment.setJobOffer(job);

        jobCommentRepository.save(comment);
        log.info("User {} commented on job {}", userId, jobId);

        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .userId(user.getId())
                .userName(user.getFirstName() + " " + user.getLastName())
                .likeCount(0)
                .likedByCurrentUser(false)
                .build();
    }

    public List<CommentResponse> getComments(Long jobId, Long currentUserId) {
        JobOffer job = findJobOrThrow(jobId);
        return jobCommentRepository.findByJobOfferOrderByCreatedAtDesc(job).stream()
                .map(c -> CommentResponse.builder()
                        .id(c.getId())
                        .content(c.getContent())
                        .createdAt(c.getCreatedAt())
                        .userId(c.getUser().getId())
                        .userName(c.getUser().getFirstName() + " " + c.getUser().getLastName())
                        .likeCount(c.getLikedBy() != null ? c.getLikedBy().size() : 0)
                        .likedByCurrentUser(currentUserId != null && c.getLikedBy() != null
                                && c.getLikedBy().stream().anyMatch(u -> u.getId().equals(currentUserId)))
                        .build())
                .collect(Collectors.toList());
    }

    public CommentResponse toggleCommentLike(Long commentId, Long userId) {
        JobComment comment = jobCommentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + commentId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        boolean alreadyLiked = comment.getLikedBy().stream().anyMatch(u -> u.getId().equals(userId));

        if (alreadyLiked) {
            comment.getLikedBy().removeIf(u -> u.getId().equals(userId));
        } else {
            comment.getLikedBy().add(user);
        }

        jobCommentRepository.save(comment);
        log.info("User {} {} comment {}", userId, alreadyLiked ? "unliked" : "liked", commentId);

        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .userId(comment.getUser().getId())
                .userName(comment.getUser().getFirstName() + " " + comment.getUser().getLastName())
                .likeCount(comment.getLikedBy().size())
                .likedByCurrentUser(!alreadyLiked)
                .build();
    }

    // ======== Helpers ========

    private JobOffer findJobOrThrow(Long id) {
        return jobOfferRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job offer not found with id: " + id));
    }

    public JobOfferResponse toResponse(JobOffer job, Long currentUserId) {
        boolean liked = currentUserId != null && job.getLikedBy().stream().anyMatch(u -> u.getId().equals(currentUserId));
        boolean applied = currentUserId != null && job.getApplications().stream()
                .anyMatch(a -> a.getStudent().getId().equals(currentUserId));

        return JobOfferResponse.builder()
                .id(job.getId())
                .title(job.getTitle())
                .description(job.getDescription())
                .company(job.getCompany())
                .location(job.getLocation())
                .createdAt(job.getCreatedAt())
                .jobType(job.getJobType() != null ? job.getJobType().toString() : null)
                .status(job.getStatus() != null ? job.getStatus().toString() : null)
                .requirements(job.getRequirements())
                .salary(job.getSalary())
                .recruiterId(job.getRecruiter() != null ? job.getRecruiter().getId() : null)
                .recruiterName(job.getRecruiter() != null ? job.getRecruiter().getFirstName() + " " + job.getRecruiter().getLastName() : null)
                .recruiterEmail(job.getRecruiter() != null ? job.getRecruiter().getEmail() : null)
                .applicationCount(job.getApplications() != null ? job.getApplications().size() : 0)
                .likeCount(job.getLikedBy() != null ? job.getLikedBy().size() : 0)
                .commentCount(job.getComments() != null ? job.getComments().size() : 0)
                .likedByCurrentUser(liked)
                .appliedByCurrentUser(applied)
                .build();
    }
}
