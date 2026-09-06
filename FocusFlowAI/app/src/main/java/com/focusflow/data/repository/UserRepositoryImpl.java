package com.focusflow.data.repository;

import com.focusflow.data.local.OnboardingPreferences;
import com.focusflow.data.local.dao.UserPreferencesDao;
import com.focusflow.data.local.entity.UserPreferencesEntity;
import com.focusflow.data.mapper.EntityMappers;
import com.focusflow.domain.model.OnboardingDraft;
import com.focusflow.domain.model.UserPreferences;
import com.focusflow.domain.repository.UserRepository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

public class UserRepositoryImpl implements UserRepository {

    private final UserPreferencesDao userPreferencesDao;
    private final OnboardingPreferences onboardingPreferences;

    public UserRepositoryImpl(
            UserPreferencesDao userPreferencesDao,
            OnboardingPreferences onboardingPreferences
    ) {
        this.userPreferencesDao = userPreferencesDao;
        this.onboardingPreferences = onboardingPreferences;
    }

    @Override
    public LiveData<UserPreferences> observePreferences() {
        return Transformations.map(userPreferencesDao.observe(), EntityMappers::toDomain);
    }

    @Override
    public void completeOnboarding(OnboardingDraft draft) {
        String name = draft.displayName() == null ? null : draft.displayName().trim();
        if (name != null && name.isEmpty()) {
            name = null;
        }
        userPreferencesDao.upsert(UserPreferencesEntity.completed(
                name,
                draft.privacyAcknowledged(),
                draft.telemetryConsented()
        ));
        onboardingPreferences.setCompleted(true);
    }

    @Override
    public void setTelemetryConsented(boolean consented) {
        UserPreferencesEntity current = userPreferencesDao.get();
        if (current == null) {
            return;
        }
        userPreferencesDao.upsert(new UserPreferencesEntity(
                current.getId(),
                current.getDisplayName(),
                current.isOnboardingCompleted(),
                current.isPrivacyAcknowledged(),
                consented,
                System.currentTimeMillis()
        ));
    }
}
