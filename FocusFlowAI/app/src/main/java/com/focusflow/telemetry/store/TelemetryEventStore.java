package com.focusflow.telemetry.store;

import com.focusflow.core.common.Constants;
import com.focusflow.data.local.dao.FocusSessionDao;
import com.focusflow.data.local.dao.TelemetryEventDao;
import com.focusflow.data.local.entity.FocusSessionEntity;
import com.focusflow.data.local.entity.TelemetryEventEntity;

/**
 * Single write path for raw events. Enforces a per-session cap for backpressure.
 */
public class TelemetryEventStore {

    private static final int MAX_EVENTS_PER_SESSION = 2000;

    private final FocusSessionDao sessionDao;
    private final TelemetryEventDao eventDao;

    public TelemetryEventStore(FocusSessionDao sessionDao, TelemetryEventDao eventDao) {
        this.sessionDao = sessionDao;
        this.eventDao = eventDao;
    }

    public void record(String type, String packageName) {
        FocusSessionEntity active = sessionDao.getActive();
        if (active == null) {
            return;
        }
        int count = eventDao.countForSession(active.getId());
        if (count >= MAX_EVENTS_PER_SESSION && Constants.EVENT_SCREEN_OFF.equals(type)) {
            return;
        }
        if (count >= MAX_EVENTS_PER_SESSION + 200) {
            return;
        }
        eventDao.insert(TelemetryEventEntity.create(active.getId(), type, packageName));
    }
}
