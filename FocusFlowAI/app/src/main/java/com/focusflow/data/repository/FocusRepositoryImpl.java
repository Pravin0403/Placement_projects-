package com.focusflow.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.focusflow.data.local.dao.FocusSessionDao;
import com.focusflow.data.local.dao.FocusSnapshotDao;
import com.focusflow.data.local.dao.UserPreferencesDao;
import com.focusflow.data.local.entity.FocusSessionEntity;
import com.focusflow.data.local.entity.FocusSnapshotEntity;
import com.focusflow.data.local.entity.UserPreferencesEntity;
import com.focusflow.data.mapper.EntityMappers;
import com.focusflow.domain.model.DashboardState;
import com.focusflow.domain.model.FocusSession;
import com.focusflow.domain.model.FocusSnapshot;
import com.focusflow.domain.model.UserPreferences;
import com.focusflow.domain.repository.FocusRepository;

public class FocusRepositoryImpl implements FocusRepository {

    private final UserPreferencesDao userPreferencesDao;
    private final FocusSnapshotDao focusSnapshotDao;
    private final FocusSessionDao focusSessionDao;

    public FocusRepositoryImpl(
            UserPreferencesDao userPreferencesDao,
            FocusSnapshotDao focusSnapshotDao,
            FocusSessionDao focusSessionDao
    ) {
        this.userPreferencesDao = userPreferencesDao;
        this.focusSnapshotDao = focusSnapshotDao;
        this.focusSessionDao = focusSessionDao;
    }

    @Override
    public LiveData<DashboardState> observeDashboard() {
        MediatorLiveData<DashboardState> mediator = new MediatorLiveData<>();
        LiveData<UserPreferencesEntity> preferencesLiveData = userPreferencesDao.observe();
        LiveData<FocusSnapshotEntity> snapshotLiveData = focusSnapshotDao.observeLatest();
        LiveData<FocusSessionEntity> activeLiveData = focusSessionDao.observeActive();

        mediator.addSource(preferencesLiveData, unused -> combine(mediator, preferencesLiveData, snapshotLiveData, activeLiveData));
        mediator.addSource(snapshotLiveData, unused -> combine(mediator, preferencesLiveData, snapshotLiveData, activeLiveData));
        mediator.addSource(activeLiveData, unused -> combine(mediator, preferencesLiveData, snapshotLiveData, activeLiveData));
        return mediator;
    }

    private void combine(
            MediatorLiveData<DashboardState> mediator,
            LiveData<UserPreferencesEntity> preferencesLiveData,
            LiveData<FocusSnapshotEntity> snapshotLiveData,
            LiveData<FocusSessionEntity> activeLiveData
    ) {
        UserPreferences preferences = EntityMappers.toDomain(preferencesLiveData.getValue());
        FocusSnapshot snapshot = EntityMappers.toDomain(snapshotLiveData.getValue());
        FocusSessionEntity activeEntity = activeLiveData.getValue();
        FocusSession active = activeEntity == null
                ? null
                : new FocusSession(activeEntity.getId(), activeEntity.getStartedAtEpochMs(), activeEntity.getEndedAtEpochMs());
        mediator.setValue(new DashboardState(preferences, snapshot, active));
    }
}
