package com.focusflow.domain.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public final class UserPreferences {

    @Nullable
    private final String displayName;
    private final boolean onboardingCompleted;
    private final boolean privacyAcknowledged;
    private final boolean telemetryConsented;

    public UserPreferences(
            @Nullable String displayName,
            boolean onboardingCompleted,
            boolean privacyAcknowledged,
            boolean telemetryConsented
    ) {
        this.displayName = displayName;
        this.onboardingCompleted = onboardingCompleted;
        this.privacyAcknowledged = privacyAcknowledged;
        this.telemetryConsented = telemetryConsented;
    }

    public static UserPreferences empty() {
        return new UserPreferences(null, false, false, false);
    }

    @Nullable
    public String displayName() {
        return displayName;
    }

    public boolean onboardingCompleted() {
        return onboardingCompleted;
    }

    public boolean privacyAcknowledged() {
        return privacyAcknowledged;
    }

    public boolean telemetryConsented() {
        return telemetryConsented;
    }

    @NonNull
    public String greetingName() {
        if (displayName == null || displayName.trim().isEmpty()) {
            return "";
        }
        return displayName.trim();
    }
}
