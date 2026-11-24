package com.campus.lostfound.model;

/**
 * Verification and review status of a claim.
 */
public enum ClaimStatus {
    PENDING("Pending Verification"),
    APPROVED("Approved"),
    REJECTED("Rejected"),
    CANCELLED("Cancelled");

    private final String displayName;

    ClaimStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static ClaimStatus fromString(String str) {
        if (str == null) return PENDING;
        try {
            return ClaimStatus.valueOf(str.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return PENDING;
        }
    }
}
