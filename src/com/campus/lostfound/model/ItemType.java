package com.campus.lostfound.model;

/**
 * Type of item reported in the system.
 */
public enum ItemType {
    LOST("Lost"),
    FOUND("Found");

    private final String displayName;

    ItemType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static ItemType fromString(String str) {
        if (str == null) return LOST;
        try {
            return ItemType.valueOf(str.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return LOST;
        }
    }
}
