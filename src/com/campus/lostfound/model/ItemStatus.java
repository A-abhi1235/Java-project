package com.campus.lostfound.model;

/**
 * Current lifecycle status of an item.
 */
public enum ItemStatus {
    OPEN("Open / Active"),
    CLAIM_PENDING("Claim Under Review"),
    CLAIMED("Claim Approved"),
    RETURNED("Returned to Owner"),
    CLOSED("Closed / Archived");

    private final String displayName;

    ItemStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static ItemStatus fromString(String str) {
        if (str == null) return OPEN;
        try {
            return ItemStatus.valueOf(str.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return OPEN;
        }
    }
}
