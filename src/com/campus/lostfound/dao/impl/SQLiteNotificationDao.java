package com.campus.lostfound.dao.impl;

import com.campus.lostfound.dao.NotificationDao;
import com.campus.lostfound.exception.DatabaseException;
import com.campus.lostfound.model.Notification;
import com.campus.lostfound.util.DatabaseManager;
import com.campus.lostfound.util.DateUtil;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SQLiteNotificationDao implements NotificationDao {
    private final DatabaseManager dbManager = DatabaseManager.getInstance();

    @Override
    public Notification create(Notification notification) throws DatabaseException {
        String sql = "INSERT INTO notifications (user_id, title, message, is_read, related_item_id, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, notification.getUserId());
            ps.setString(2, notification.getTitle());
            ps.setString(3, notification.getMessage());
            ps.setInt(4, notification.isRead() ? 1 : 0);
            if (notification.getRelatedItemId() != null) {
                ps.setInt(5, notification.getRelatedItemId());
            } else {
                ps.setNull(5, Types.INTEGER);
            }
            ps.setString(6, DateUtil.formatIsoDateTime(notification.getCreatedAt() != null ? notification.getCreatedAt() : LocalDateTime.now()));

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        notification.setId(rs.getInt(1));
                    }
                }
            }
            return notification;
        } catch (SQLException e) {
            throw new DatabaseException("Error creating notification: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Notification> findByUserId(int userId) throws DatabaseException {
        List<Notification> list = new ArrayList<>();
        String sql = "SELECT * FROM notifications WHERE user_id = ? ORDER BY created_at DESC LIMIT 50";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int relatedItem = rs.getInt("related_item_id");
                    Integer relatedItemId = rs.wasNull() ? null : relatedItem;
                    Notification n = new Notification(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getString("title"),
                        rs.getString("message"),
                        rs.getInt("is_read") == 1,
                        relatedItemId,
                        DateUtil.parseDateTime(rs.getString("created_at"))
                    );
                    list.add(n);
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Error fetching notifications: " + e.getMessage(), e);
        }
    }

    @Override
    public int countUnreadByUserId(int userId) throws DatabaseException {
        String sql = "SELECT COUNT(*) FROM notifications WHERE user_id = ? AND is_read = 0";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error counting unread notifications: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean markAsRead(int notificationId) throws DatabaseException {
        String sql = "UPDATE notifications SET is_read = 1 WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, notificationId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error marking notification as read: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean markAllAsRead(int userId) throws DatabaseException {
        String sql = "UPDATE notifications SET is_read = 1 WHERE user_id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error marking all notifications as read: " + e.getMessage(), e);
        }
    }
}
