package com.focusflow.domain.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public final class DashboardState {

    @NonNull
    private final UserPreferences userPreferences;
    @NonNull
    private final FocusSnapshot snapshot;
    @Nullable
    private final FocusSession activeSession;

    public DashboardState(
            @NonNull UserPreferences userPreferences,
            @NonNull FocusSnapshot snapshot,
            @Nullable FocusSession activeSession
    ) {
        this.userPreferences = userPreferences;
        this.snapshot = snapshot;
        this.activeSession = activeSession;
    }

    @NonNull
    public UserPreferences userPreferences() {
        return userPreferences;
    }

    @NonNull
    public FocusSnapshot snapshot() {
        return snapshot;
    }

    @Nullable
    public FocusSession activeSession() {
        return activeSession;
    }

    public boolean sessionActive() {
        return activeSession != null && activeSession.endedAtEpochMs() == null;
    }
}
