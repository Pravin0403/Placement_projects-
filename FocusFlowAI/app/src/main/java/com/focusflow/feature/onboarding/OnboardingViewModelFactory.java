package com.focusflow.feature.onboarding;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.focusflow.core.di.AppContainer;

public class OnboardingViewModelFactory implements ViewModelProvider.Factory {

    private final AppContainer appContainer;

    public OnboardingViewModelFactory(AppContainer appContainer) {
        this.appContainer = appContainer;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(OnboardingViewModel.class)) {
            return (T) new OnboardingViewModel(
                    appContainer.completeOnboardingUseCase(),
                    appContainer.executors()
            );
        }
        throw new IllegalArgumentException("Unknown ViewModel: " + modelClass.getName());
    }
}
