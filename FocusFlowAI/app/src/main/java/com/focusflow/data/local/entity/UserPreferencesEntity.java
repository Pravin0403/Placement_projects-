package com.focusflow.data.local.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.focusflow.core.common.Constants;

@Entity(tableName = "user_preferences")
public class UserPreferencesEntity {

    @PrimaryKey
    private int id;

    @Nullable
    private String displayName;

    private boolean onboardingCompleted;
    private boolean privacyAcknowledged;
    private boolean telemetryConsented;
    private long updatedAtEpochMs;

    public UserPreferencesEntity(
            int id,
            @Nullable String displayName,
            boolean onboardingCompleted,
            boolean privacyAcknowledged,
            boolean telemetryConsented,
            long updatedAtEpochMs
    ) {
        this.id = id;
        this.displayName = displayName;
        this.onboardingCompleted = onboardingCompleted;
        this.privacyAcknowledged = privacyAcknowledged;
        this.telemetryConsented = telemetryConsented;
        this.updatedAtEpochMs = updatedAtEpochMs;
    }

    @NonNull
    public static UserPreferencesEntity completed(
            @Nullable String displayName,
            boolean privacyAcknowledged,
            boolean telemetryConsented
    ) {
        return new UserPreferencesEntity(
                Constants.USER_PREFERENCES_ROW_ID,
                displayName,
                true,
                privacyAcknowledged,
                telemetryConsented,
                System.currentTimeMillis()
        );
    }

    public int getId() {
        return id;
    }

    @Nullable
    public String getDisplayName() {
        return displayName;
    }

    public boolean isOnboardingCompleted() {
        return onboardingCompleted;
    }

    public boolean isPrivacyAcknowledged() {
        return privacyAcknowledged;
    }

    public boolean isTelemetryConsented() {
        return telemetryConsented;
    }

    public long getUpdatedAtEpochMs() {
        return updatedAtEpochMs;
    }
}
