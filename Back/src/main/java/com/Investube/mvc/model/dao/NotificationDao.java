package com.Investube.mvc.model.dao;

import java.util.List;
import java.util.Map;

import com.Investube.mvc.model.dto.Notification;

public interface NotificationDao {
    int insertNotification(Map<String, Object> params);
    List<Notification> selectByRecipient(Map<String, Object> params);
    List<Notification> selectRecentByRecipient(int recipientId);
    int selectUnreadCount(int recipientId);
    int markAsRead(Map<String, Object> params);
    int markAllAsRead(int recipientId);
}
