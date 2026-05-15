package tn.entreprise.escproject.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import tn.entreprise.escproject.dto.NotificationDTO;
import tn.entreprise.escproject.entite.*;
import tn.entreprise.escproject.exception.ResourceNotFoundException;
import tn.entreprise.escproject.exception.UnauthorizedException;
import tn.entreprise.escproject.repositories.JobAppNotificationRepository;
import tn.entreprise.escproject.repositories.UserRepository;
import tn.entreprise.escproject.services.Interfaces.INotificationService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class NotificationServiceImp implements INotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImp.class);

    @Autowired
    private JobAppNotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // ======== Public trigger methods (called by ApplicationService) ========

    public void notifyRecruiterNewApplication(User recruiter, User student, JobOffer job, Application application) {
        String message = student.getFirstName() + " " + student.getLastName()
                + " applied to your job post \"" + job.getTitle() + "\"";

        JobAppNotification notification = buildNotification(
                recruiter, NotificationType.NEW_APPLICATION, message,
                job.getId(), application.getId()
        );
        notificationRepository.save(notification);
        log.info("Notification created for recruiter {} — new application on job {}", recruiter.getId(), job.getId());
        pushToUser(recruiter.getEmail(), toDTO(notification));
    }

    public void notifyStudentStatusChanged(User student, JobOffer job, Application application) {
        String message = "Your application for \"" + job.getTitle() + "\" at " + job.getCompany()
                + " has been updated to: " + application.getStatus().toString();

        JobAppNotification notification = buildNotification(
                student, NotificationType.APPLICATION_STATUS_CHANGED, message,
                job.getId(), application.getId()
        );
        notificationRepository.save(notification);
        log.info("Notification created for student {} — status changed on application {}", student.getId(), application.getId());
        pushToUser(student.getEmail(), toDTO(notification));
    }

    // ======== INotificationService ========

    @Override
    public List<NotificationDTO> getMyNotifications(Long userId) {
        User user = findUserOrThrow(userId);
        return notificationRepository.findByRecipientOrderByCreatedAtDesc(user)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public NotificationDTO markAsRead(Long notificationId, Long userId) {
        JobAppNotification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + notificationId));

        if (!notification.getRecipient().getId().equals(userId)) {
            throw new UnauthorizedException("You can only manage your own notifications");
        }

        notification.setRead(true);
        notificationRepository.save(notification);
        return toDTO(notification);
    }

    @Override
    public Map<String, Long> markAllAsRead(Long userId) {
        User user = findUserOrThrow(userId);
        List<JobAppNotification> unread = notificationRepository.findByRecipientAndReadFalseOrderByCreatedAtDesc(user);
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
        log.info("Marked {} notifications as read for user {}", unread.size(), userId);
        return Map.of("markedRead", (long) unread.size());
    }

    @Override
    public long getUnreadCount(Long userId) {
        User user = findUserOrThrow(userId);
        return notificationRepository.countByRecipientAndReadFalse(user);
    }

    // ======== Helpers ========

    private JobAppNotification buildNotification(User recipient, NotificationType type,
                                                  String message, Long jobId, Long applicationId) {
        JobAppNotification notification = new JobAppNotification();
        notification.setRecipient(recipient);
        notification.setType(type);
        notification.setMessage(message);
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setRelatedJobId(jobId);
        notification.setRelatedApplicationId(applicationId);
        return notification;
    }

    private void pushToUser(String email, NotificationDTO dto) {
        try {
            messagingTemplate.convertAndSendToUser(email, "/queue/notifications", dto);
        } catch (Exception e) {
            log.warn("Failed to push real-time notification to {}: {}", email, e.getMessage());
        }
    }

    private User findUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }

    public NotificationDTO toDTO(JobAppNotification notification) {
        return NotificationDTO.builder()
                .id(notification.getId())
                .type(notification.getType().toString())
                .message(notification.getMessage())
                .read(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .relatedJobId(notification.getRelatedJobId())
                .relatedApplicationId(notification.getRelatedApplicationId())
                .recipientId(notification.getRecipient().getId())
                .build();
    }
}
