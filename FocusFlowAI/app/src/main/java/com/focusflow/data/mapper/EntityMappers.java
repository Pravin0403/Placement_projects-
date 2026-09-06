package com.focusflow.data.mapper;

import androidx.annotation.Nullable;

import com.focusflow.data.local.entity.FocusSnapshotEntity;
import com.focusflow.data.local.entity.UserPreferencesEntity;
import com.focusflow.domain.model.FocusSnapshot;
import com.focusflow.domain.model.UserPreferences;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class EntityMappers {

    private EntityMappers() {
    }

    public static UserPreferences toDomain(@Nullable UserPreferencesEntity entity) {
        if (entity == null) {
            return UserPreferences.empty();
        }
        return new UserPreferences(
                entity.getDisplayName(),
                entity.isOnboardingCompleted(),
                entity.isPrivacyAcknowledged(),
                entity.isTelemetryConsented()
        );
    }

    public static FocusSnapshot toDomain(@Nullable FocusSnapshotEntity entity) {
        if (entity == null) {
            return FocusSnapshot.empty();
        }
        return new FocusSnapshot(
                entity.getFocusScore(),
                entity.getDistractionRiskPercent(),
                entity.getSessionStartedAtEpochMs(),
                entity.getSessionEndedAtEpochMs(),
                splitSignals(entity.getTopSignalsCsv()),
                entity.getRecommendation()
        );
    }

    private static List<String> splitSignals(@Nullable String csv) {
        if (csv == null || csv.trim().isEmpty()) {
            return Collections.emptyList();
        }
        String[] parts = csv.split(",");
        List<String> signals = new ArrayList<>();
        for (String part : parts) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                signals.add(trimmed);
            }
        }
        return signals;
    }
}
