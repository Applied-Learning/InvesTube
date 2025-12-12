package com.Investube.mvc.model.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.Investube.mvc.model.dao.NotificationDao;
import com.Investube.mvc.model.dto.Notification;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationDao notificationDao;

    public NotificationServiceImpl(NotificationDao notificationDao) {
        this.notificationDao = notificationDao;
    }

    @Override
    public boolean createNotification(Notification notification) {
        Map<String, Object> params = new HashMap<>();
        params.put("recipientId", notification.getRecipientId());
        params.put("actorId", notification.getActorId());
        params.put("type", notification.getType());
        params.put("targetType", notification.getTargetType());
        params.put("targetId", notification.getTargetId());
        params.put("message", notification.getMessage());
        int res = notificationDao.insertNotification(params);
        return res > 0;
    }

    @Override
    public List<Notification> getNotificationsForUser(int recipientId) {
        Map<String, Object> params = new HashMap<>();
        params.put("recipientId", recipientId);
        params.put("limit", 50);
        params.put("offset", 0);
        return notificationDao.selectByRecipient(params);
    }

    @Override
    public int getUnreadCount(int recipientId) {
        return notificationDao.selectUnreadCount(recipientId);
    }

    @Override
    public boolean markAsRead(int id, int recipientId) {
        Map<String, Object> params = new HashMap<>();
        params.put("id", id);
        params.put("recipientId", recipientId);
        return notificationDao.markAsRead(params) > 0;
    }

    @Override
    public boolean markAllAsRead(int recipientId) {
        return notificationDao.markAllAsRead(recipientId) > 0;
    }
}
