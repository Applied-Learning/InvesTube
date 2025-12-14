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

    @Override
    public Map<String, Boolean> getSettingsForUser(int userId) {
        List<Map<String, Object>> rows = notificationDao.selectSettingsByUser(userId);
        Map<String, Boolean> settings = new HashMap<>();
        if (rows != null) {
            for (Map<String, Object> row : rows) {
                Object typeObj = row.get("type");
                Object enabledObj = row.get("enabled");
                if (typeObj != null && enabledObj != null) {
                    settings.put(String.valueOf(typeObj), Boolean.TRUE.equals(enabledObj) || "1".equals(String.valueOf(enabledObj)));
                }
            }
        }
        return settings;
    }

    @Override
    public void saveSettingsForUser(int userId, Map<String, Boolean> settings) {
        if (settings == null) return;
        for (Map.Entry<String, Boolean> e : settings.entrySet()) {
            Map<String, Object> params = new HashMap<>();
            params.put("userId", userId);
            params.put("type", e.getKey());
            params.put("enabled", e.getValue() != null ? e.getValue() : Boolean.TRUE);
            notificationDao.upsertSetting(params);
        }
    }

    @Override
    public boolean isNotificationEnabled(int userId, String type) {
        if (userId <= 0 || type == null) return true; // 기본 ON
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("type", type);
        Boolean enabled = notificationDao.isNotificationEnabled(params);
        // 설정이 없으면 기본 ON, 값이 있으면 해당 값 사용
        return enabled == null ? true : enabled;
    }
}
