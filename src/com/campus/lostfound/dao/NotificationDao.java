package com.campus.lostfound.dao;

import com.campus.lostfound.exception.DatabaseException;
import com.campus.lostfound.model.Notification;

import java.util.List;

/**
 * Data Access Object interface for Notifications.
 */
public interface NotificationDao {
    Notification create(Notification notification) throws DatabaseException;
    List<Notification> findByUserId(int userId) throws DatabaseException;
    int countUnreadByUserId(int userId) throws DatabaseException;
    boolean markAsRead(int notificationId) throws DatabaseException;
    boolean markAllAsRead(int userId) throws DatabaseException;
}
