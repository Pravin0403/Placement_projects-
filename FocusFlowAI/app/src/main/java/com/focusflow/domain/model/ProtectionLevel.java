package com.focusflow.domain.model;

/**
 * Represents the protection level for focus sessions.
 */
public enum ProtectionLevel {
    LIGHT("Light Protection"),
    STRONG("Strong Protection"),
    DEEP("Deep Protection");

    private final String displayName;

    ProtectionLevel(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}