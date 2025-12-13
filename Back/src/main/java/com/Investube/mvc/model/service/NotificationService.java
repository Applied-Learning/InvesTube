package com.Investube.mvc.model.service;

import java.util.List;
import java.util.Map;

import com.Investube.mvc.model.dto.Notification;

public interface NotificationService {
    boolean createNotification(Notification notification);
    List<Notification> getNotificationsForUser(int recipientId);
    int getUnreadCount(int recipientId);
    boolean markAsRead(int id, int recipientId);
    boolean markAllAsRead(int recipientId);

    // settings
    Map<String, Boolean> getSettingsForUser(int userId);
    void saveSettingsForUser(int userId, Map<String, Boolean> settings);
    boolean isNotificationEnabled(int userId, String type);
}
