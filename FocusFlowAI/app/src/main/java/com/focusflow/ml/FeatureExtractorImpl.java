package com.focusflow.ml;

import com.focusflow.core.common.Constants;
import com.focusflow.data.local.entity.TelemetryEventEntity;

import java.util.List;

public class FeatureExtractorImpl implements FeatureExtractor {

    @Override
    public java.util.Map<String, Float> extract() {
        return java.util.Collections.emptyMap();
    }

    public FeatureVector extract(
            List<TelemetryEventEntity> events,
            long windowStartEpochMs,
            long sessionStartEpochMs,
            long windowEndEpochMs,
            long previousBreakMs,
            float previousRisk
    ) {
        int appSwitches = 0;
        int unlocks = 0;
        int screenOff = 0;
        for (TelemetryEventEntity event : events) {
            if (Constants.EVENT_APP_SWITCH.equals(event.getType())) {
                appSwitches++;
            } else if (Constants.EVENT_SCREEN_UNLOCK.equals(event.getType())) {
                unlocks++;
            } else if (Constants.EVENT_SCREEN_OFF.equals(event.getType())) {
                screenOff++;
            }
        }
        float windowDurationMin = Math.max(1f, (windowEndEpochMs - windowStartEpochMs) / 60000f);
        float elapsedMin = Math.max(1f, (windowEndEpochMs - sessionStartEpochMs) / 60000f);
        float interactions = (appSwitches + unlocks) / windowDurationMin;
        return FeatureVector.fromWindow(
                appSwitches,
                unlocks,
                windowDurationMin,
                elapsedMin,
                interactions,
                previousBreakMs / 60000f,
                windowEndEpochMs,
                screenOff,
                previousRisk
        );
    }
}
