package com.focusflow.domain.repository;

import androidx.lifecycle.LiveData;

import com.focusflow.domain.model.OnboardingDraft;
import com.focusflow.domain.model.UserPreferences;

public interface UserRepository {

    LiveData<UserPreferences> observePreferences();

    void completeOnboarding(OnboardingDraft draft);

    void setTelemetryConsented(boolean consented);
}
