package com.campus.lostfound.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Concrete class representing a Found Item.
 * Demonstrates OOP Inheritance extending BaseItem.
 */
public class FoundItem extends BaseItem {
    private String storageLocation; // e.g. "Security Office Locker 12", "CS Department Reception"
    private String verificationQuestion; // Security question to confirm rightful owner

    public FoundItem() {
        super();
    }

    public FoundItem(int id, String title, String description, int categoryId,
                     String location, LocalDate dateReported, String imageUrl,
                     ItemStatus status, int reportedByUserId, String contactPhone,
                     String contactEmail, LocalDateTime createdAt, LocalDateTime updatedAt,
                     String storageLocation, String verificationQuestion) {
        super(id, title, description, categoryId, location, dateReported, imageUrl,
              status, reportedByUserId, contactPhone, contactEmail, createdAt, updatedAt);
        this.storageLocation = storageLocation;
        this.verificationQuestion = verificationQuestion;
    }

    @Override
    public ItemType getItemType() {
        return ItemType.FOUND;
    }

    @Override
    public String getDetailsSummary() {
        return "Found at " + location + " on " + dateReported +
               (storageLocation != null && !storageLocation.isEmpty() ? " (Stored at: " + storageLocation + ")" : "");
    }

    @Override
    public String getSpecificAttributeLabel() {
        return "Custody / Storage Location";
    }

    @Override
    public String getSpecificAttributeValue() {
        return storageLocation != null && !storageLocation.trim().isEmpty() ? storageLocation : "With Finder";
    }

    public String getStorageLocation() { return storageLocation; }
    public void setStorageLocation(String storageLocation) { this.storageLocation = storageLocation; }

    public String getVerificationQuestion() { return verificationQuestion; }
    public void setVerificationQuestion(String verificationQuestion) { this.verificationQuestion = verificationQuestion; }
}
