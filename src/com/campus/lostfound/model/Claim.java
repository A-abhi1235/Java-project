package com.campus.lostfound.model;

import java.time.LocalDateTime;

/**
 * Represents a claim submitted by a student/user to claim a found item.
 */
public class Claim {
    private int id;
    private int itemId;
    private String itemTitle;          // Helper display
    private ItemType itemType;         // Helper display
    private int claimantId;
    private String claimantName;       // Helper display
    private String claimantEmail;      // Helper display
    private String claimantPhone;      // Helper display
    private String proofDetails;       // Verification answer / proof of ownership / marks / serial #
    private ClaimStatus status;
    private String reviewerNotes;      // Admin or finder resolution notes
    private Integer reviewedByUserId;  // Admin or item owner who approved/rejected
    private LocalDateTime createdAt;
    private LocalDateTime reviewedAt;

    public Claim() {
        this.status = ClaimStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    public Claim(int id, int itemId, int claimantId, String proofDetails,
                 ClaimStatus status, String reviewerNotes, Integer reviewedByUserId,
                 LocalDateTime createdAt, LocalDateTime reviewedAt) {
        this.id = id;
        this.itemId = itemId;
        this.claimantId = claimantId;
        this.proofDetails = proofDetails;
        this.status = status != null ? status : ClaimStatus.PENDING;
        this.reviewerNotes = reviewerNotes;
        this.reviewedByUserId = reviewedByUserId;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.reviewedAt = reviewedAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }

    public String getItemTitle() { return itemTitle; }
    public void setItemTitle(String itemTitle) { this.itemTitle = itemTitle; }

    public ItemType getItemType() { return itemType; }
    public void setItemType(ItemType itemType) { this.itemType = itemType; }

    public int getClaimantId() { return claimantId; }
    public void setClaimantId(int claimantId) { this.claimantId = claimantId; }

    public String getClaimantName() { return claimantName; }
    public void setClaimantName(String claimantName) { this.claimantName = claimantName; }

    public String getClaimantEmail() { return claimantEmail; }
    public void setClaimantEmail(String claimantEmail) { this.claimantEmail = claimantEmail; }

    public String getClaimantPhone() { return claimantPhone; }
    public void setClaimantPhone(String claimantPhone) { this.claimantPhone = claimantPhone; }

    public String getProofDetails() { return proofDetails; }
    public void setProofDetails(String proofDetails) { this.proofDetails = proofDetails; }

    public ClaimStatus getStatus() { return status; }
    public void setStatus(ClaimStatus status) { this.status = status; }

    public String getReviewerNotes() { return reviewerNotes; }
    public void setReviewerNotes(String reviewerNotes) { this.reviewerNotes = reviewerNotes; }

    public Integer getReviewedByUserId() { return reviewedByUserId; }
    public void setReviewedByUserId(Integer reviewedByUserId) { this.reviewedByUserId = reviewedByUserId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }
}
