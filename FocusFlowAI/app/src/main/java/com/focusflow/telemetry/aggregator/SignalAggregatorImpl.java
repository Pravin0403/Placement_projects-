package com.focusflow.telemetry.aggregator;

import com.focusflow.core.common.Constants;
import com.focusflow.data.local.dao.FocusSessionDao;
import com.focusflow.data.local.dao.TelemetryEventDao;
import com.focusflow.data.local.entity.FocusSessionEntity;
import com.focusflow.data.local.entity.TelemetryEventEntity;
import com.focusflow.ml.FeatureExtractorImpl;
import com.focusflow.ml.FeatureVector;

import java.util.Collections;
import java.util.List;

public class SignalAggregatorImpl implements SignalAggregator {

    private final FocusSessionDao sessionDao;
    private final TelemetryEventDao eventDao;
    private final FeatureExtractorImpl extractor;

    public SignalAggregatorImpl(
            FocusSessionDao sessionDao,
            TelemetryEventDao eventDao,
            FeatureExtractorImpl extractor
    ) {
        this.sessionDao = sessionDao;
        this.eventDao = eventDao;
        this.extractor = extractor;
    }

    @Override
    public void aggregateCurrentWindow() {
        // Aggregation is pulled by FocusEngine; this keeps the interface for tests.
    }

    public FeatureVector aggregate(long previousBreakMs, float previousRisk) {
        FocusSessionEntity session = sessionDao.getActive();
        if (session == null) {
            return null;
        }
        long now = System.currentTimeMillis();
        long windowStart = Math.max(session.getStartedAtEpochMs(), now - Constants.WINDOW_DURATION_MS);
        List<TelemetryEventEntity> events = eventDao.eventsInRange(session.getId(), windowStart, now);
        if (events == null) {
            events = Collections.emptyList();
        }
        return extractor.extract(
                events,
                windowStart,
                session.getStartedAtEpochMs(),
                now,
                previousBreakMs,
                previousRisk
        );
    }
}
