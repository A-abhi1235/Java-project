package com.campus.lostfound.model;

/**
 * User roles in the smart campus system.
 */
public enum Role {
    STUDENT("Student"),
    FACULTY("Faculty / Staff"),
    ADMIN("Campus Security Admin");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static Role fromString(String roleStr) {
        if (roleStr == null) return STUDENT;
        try {
            return Role.valueOf(roleStr.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return STUDENT;
        }
    }
}
