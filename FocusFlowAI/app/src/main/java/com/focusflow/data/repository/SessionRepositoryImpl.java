package com.focusflow.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.focusflow.core.common.Constants;
import com.focusflow.data.local.dao.FocusSessionDao;
import com.focusflow.data.local.dao.TelemetryEventDao;
import com.focusflow.data.local.entity.FocusSessionEntity;
import com.focusflow.data.local.entity.TelemetryEventEntity;
import com.focusflow.domain.model.FocusSession;
import com.focusflow.domain.repository.SessionRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SessionRepositoryImpl implements SessionRepository {

    private final FocusSessionDao focusSessionDao;
    private final TelemetryEventDao telemetryEventDao;

    public SessionRepositoryImpl(FocusSessionDao focusSessionDao, TelemetryEventDao telemetryEventDao) {
        this.focusSessionDao = focusSessionDao;
        this.telemetryEventDao = telemetryEventDao;
    }

    @Override
    public LiveData<List<FocusSession>> observeSessions() {
        return Transformations.map(focusSessionDao.observeAll(), this::toDomainList);
    }

    @Override
    public LiveData<FocusSession> observeActive() {
        return Transformations.map(focusSessionDao.observeActive(), this::toDomain);
    }

    @Override
    public long startSession() {
        FocusSessionEntity existing = focusSessionDao.getActive();
        if (existing != null) {
            return existing.getId();
        }
        long id = focusSessionDao.insert(new FocusSessionEntity(0, System.currentTimeMillis(), null));
        telemetryEventDao.insert(TelemetryEventEntity.create(id, Constants.EVENT_SESSION_START, null));
        return id;
    }

    @Override
    public void endSession() {
        FocusSessionEntity active = focusSessionDao.getActive();
        if (active == null) {
            return;
        }
        long now = System.currentTimeMillis();
        active.setEndedAtEpochMs(now);
        focusSessionDao.update(active);
        telemetryEventDao.insert(TelemetryEventEntity.create(active.getId(), Constants.EVENT_SESSION_END, null));
    }

    private List<FocusSession> toDomainList(List<FocusSessionEntity> entities) {
        if (entities == null || entities.isEmpty()) {
            return Collections.emptyList();
        }
        List<FocusSession> sessions = new ArrayList<>(entities.size());
        for (FocusSessionEntity entity : entities) {
            sessions.add(toDomain(entity));
        }
        return sessions;
    }

    private FocusSession toDomain(FocusSessionEntity entity) {
        if (entity == null) {
            return null;
        }
        return new FocusSession(entity.getId(), entity.getStartedAtEpochMs(), entity.getEndedAtEpochMs());
    }
}
