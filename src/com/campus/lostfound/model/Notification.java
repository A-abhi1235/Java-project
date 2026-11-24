package com.campus.lostfound.model;

import java.time.LocalDateTime;

/**
 * In-app notification for students regarding their items and claims.
 */
public class Notification {
    private int id;
    private int userId;
    private String title;
    private String message;
    private boolean isRead;
    private Integer relatedItemId;
    private LocalDateTime createdAt;

    public Notification() {
        this.isRead = false;
        this.createdAt = LocalDateTime.now();
    }

    public Notification(int id, int userId, String title, String message,
                        boolean isRead, Integer relatedItemId, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.isRead = isRead;
        this.relatedItemId = relatedItemId;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }

    public Integer getRelatedItemId() { return relatedItemId; }
    public void setRelatedItemId(Integer relatedItemId) { this.relatedItemId = relatedItemId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
