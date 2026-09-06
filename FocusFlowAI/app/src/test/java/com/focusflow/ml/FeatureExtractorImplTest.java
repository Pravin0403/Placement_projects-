package com.focusflow.ml;

import com.focusflow.core.common.Constants;
import com.focusflow.data.local.entity.TelemetryEventEntity;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class FeatureExtractorImplTest {

    @Test
    public void extract_usesWindowDurationForWindowSignals() {
        FeatureExtractorImpl extractor = new FeatureExtractorImpl();
        long minute = 60_000L;
        FeatureVector vector = extractor.extract(
                Arrays.asList(
                        event(Constants.EVENT_APP_SWITCH, 3 * minute),
                        event(Constants.EVENT_APP_SWITCH, 4 * minute),
                        event(Constants.EVENT_SCREEN_UNLOCK, 4 * minute)
                ),
                3 * minute,
                0L,
                5 * minute,
                0L,
                0f
        );

        assertEquals(2f, vector.get(2), 0.001f);
        assertEquals(5f, vector.get(3), 0.001f);
        assertEquals(1.5f, vector.get(4), 0.001f);
    }

    private static TelemetryEventEntity event(String type, long timestamp) {
        return new TelemetryEventEntity(0, type + timestamp, 1L, type, null, timestamp, false);
    }
}
