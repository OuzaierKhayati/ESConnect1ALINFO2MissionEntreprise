package tn.entreprise.escproject.services.Interfaces;

import tn.entreprise.escproject.dto.NotificationDTO;

import java.util.List;
import java.util.Map;

public interface INotificationService {
    List<NotificationDTO> getMyNotifications(Long userId);
    NotificationDTO markAsRead(Long notificationId, Long userId);
    Map<String, Long> markAllAsRead(Long userId);
    long getUnreadCount(Long userId);
}
