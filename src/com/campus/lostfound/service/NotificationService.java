package com.campus.lostfound.service;

import com.campus.lostfound.dao.NotificationDao;
import com.campus.lostfound.dao.impl.SQLiteNotificationDao;
import com.campus.lostfound.exception.DatabaseException;
import com.campus.lostfound.model.Notification;

import java.util.Collections;
import java.util.List;

public class NotificationService {
    private final NotificationDao notificationDao;

    public NotificationService() {
        this.notificationDao = new SQLiteNotificationDao();
    }

    public NotificationService(NotificationDao notificationDao) {
        this.notificationDao = notificationDao;
    }

    public List<Notification> getUserNotifications(int userId) {
        try {
            return notificationDao.findByUserId(userId);
        } catch (DatabaseException e) {
            System.err.println("Error fetching notifications: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public int getUnreadCount(int userId) {
        try {
            return notificationDao.countUnreadByUserId(userId);
        } catch (DatabaseException e) {
            return 0;
        }
    }

    public void sendNotification(int userId, String title, String message, Integer relatedItemId) {
        try {
            Notification n = new Notification();
            n.setUserId(userId);
            n.setTitle(title);
            n.setMessage(message);
            n.setRelatedItemId(relatedItemId);
            notificationDao.create(n);
        } catch (DatabaseException e) {
            System.err.println("Could not create notification: " + e.getMessage());
        }
    }

    public boolean markAsRead(int notificationId) {
        try {
            return notificationDao.markAsRead(notificationId);
        } catch (DatabaseException e) {
            return false;
        }
    }

    public boolean markAllAsRead(int userId) {
        try {
            return notificationDao.markAllAsRead(userId);
        } catch (DatabaseException e) {
            return false;
        }
    }
}
