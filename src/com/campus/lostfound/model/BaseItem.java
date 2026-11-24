package com.campus.lostfound.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Abstract base class representing an item reported in the system.
 * Demonstrates Object-Oriented Programming (OOP) Abstraction and Encapsulation.
 */
public abstract class BaseItem {
    protected int id;
    protected String title;
    protected String description;
    protected int categoryId;
    protected String categoryName; // Helper for display
    protected String location;
    protected LocalDate dateReported; // Date when lost/found occurred
    protected String imageUrl;
    protected ItemStatus status;
    protected int reportedByUserId;
    protected String reportedByUserName; // Helper for display
    protected String contactPhone;
    protected String contactEmail;
    protected LocalDateTime createdAt;
    protected LocalDateTime updatedAt;

    public BaseItem() {
        this.status = ItemStatus.OPEN;
        this.dateReported = LocalDate.now();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public BaseItem(int id, String title, String description, int categoryId,
                    String location, LocalDate dateReported, String imageUrl,
                    ItemStatus status, int reportedByUserId, String contactPhone,
                    String contactEmail, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.categoryId = categoryId;
        this.location = location;
        this.dateReported = dateReported != null ? dateReported : LocalDate.now();
        this.imageUrl = imageUrl;
        this.status = status != null ? status : ItemStatus.OPEN;
        this.reportedByUserId = reportedByUserId;
        this.contactPhone = contactPhone;
        this.contactEmail = contactEmail;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.updatedAt = updatedAt != null ? updatedAt : LocalDateTime.now();
    }

    // Abstract methods demonstrating Polymorphism
    public abstract ItemType getItemType();
    public abstract String getDetailsSummary();
    public abstract String getSpecificAttributeLabel();
    public abstract String getSpecificAttributeValue();

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public LocalDate getDateReported() { return dateReported; }
    public void setDateReported(LocalDate dateReported) { this.dateReported = dateReported; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public ItemStatus getStatus() { return status; }
    public void setStatus(ItemStatus status) { this.status = status; }

    public int getReportedByUserId() { return reportedByUserId; }
    public void setReportedByUserId(int reportedByUserId) { this.reportedByUserId = reportedByUserId; }

    public String getReportedByUserName() { return reportedByUserName; }
    public void setReportedByUserName(String reportedByUserName) { this.reportedByUserName = reportedByUserName; }

    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }

    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public boolean isOpen() {
        return this.status == ItemStatus.OPEN;
    }
}
