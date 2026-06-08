package tn.entreprise.escproject.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import tn.entreprise.escproject.dto.ApiResponse;
import tn.entreprise.escproject.dto.NotificationDTO;
import tn.entreprise.escproject.entite.User;
import tn.entreprise.escproject.exception.ResourceNotFoundException;
import tn.entreprise.escproject.repositories.UserRepository;
import tn.entreprise.escproject.services.NotificationServiceImp;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    @Autowired
    private NotificationServiceImp notificationService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<NotificationDTO>>> getMyNotifications(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        List<NotificationDTO> notifications = notificationService.getMyNotifications(user.getId());
        return ResponseEntity.ok(new ApiResponse<>(true, "Notifications retrieved", notifications));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getUnreadCount(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        long count = notificationService.getUnreadCount(user.getId());
        return ResponseEntity.ok(new ApiResponse<>(true, "Unread count retrieved", Map.of("count", count)));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<NotificationDTO>> markAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        NotificationDTO dto = notificationService.markAsRead(id, user.getId());
        return ResponseEntity.ok(new ApiResponse<>(true, "Notification marked as read", dto));
    }

    @PutMapping("/read-all")
    public ResponseEntity<ApiResponse<Map<String, Long>>> markAllAsRead(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        Map<String, Long> result = notificationService.markAllAsRead(user.getId());
        return ResponseEntity.ok(new ApiResponse<>(true, "All notifications marked as read", result));
    }

    private User getCurrentUser(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
