package com.campus.lostfound.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Concrete class representing a Lost Item.
 * Demonstrates OOP Inheritance extending BaseItem.
 */
public class LostItem extends BaseItem {
    private double rewardAmount;
    private String lastSeenDetails;

    public LostItem() {
        super();
        this.rewardAmount = 0.0;
    }

    public LostItem(int id, String title, String description, int categoryId,
                    String location, LocalDate dateReported, String imageUrl,
                    ItemStatus status, int reportedByUserId, String contactPhone,
                    String contactEmail, LocalDateTime createdAt, LocalDateTime updatedAt,
                    double rewardAmount, String lastSeenDetails) {
        super(id, title, description, categoryId, location, dateReported, imageUrl,
              status, reportedByUserId, contactPhone, contactEmail, createdAt, updatedAt);
        this.rewardAmount = rewardAmount;
        this.lastSeenDetails = lastSeenDetails;
    }

    @Override
    public ItemType getItemType() {
        return ItemType.LOST;
    }

    @Override
    public String getDetailsSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("Lost at ").append(location).append(" on ").append(dateReported);
        if (rewardAmount > 0) {
            sb.append(" (Reward: $").append(String.format("%.2f", rewardAmount)).append(")");
        }
        return sb.toString();
    }

    @Override
    public String getSpecificAttributeLabel() {
        return "Reward / Last Seen";
    }

    @Override
    public String getSpecificAttributeValue() {
        if (rewardAmount > 0 && lastSeenDetails != null && !lastSeenDetails.trim().isEmpty()) {
            return "$" + String.format("%.2f", rewardAmount) + " | " + lastSeenDetails;
        } else if (rewardAmount > 0) {
            return "Reward: $" + String.format("%.2f", rewardAmount);
        } else if (lastSeenDetails != null && !lastSeenDetails.trim().isEmpty()) {
            return lastSeenDetails;
        }
        return "None";
    }

    public double getRewardAmount() { return rewardAmount; }
    public void setRewardAmount(double rewardAmount) { this.rewardAmount = rewardAmount; }

    public String getLastSeenDetails() { return lastSeenDetails; }
    public void setLastSeenDetails(String lastSeenDetails) { this.lastSeenDetails = lastSeenDetails; }
}
