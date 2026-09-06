package com.focusflow.feature.settings;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.focusflow.core.di.AppContainer;

public class SettingsViewModelFactory implements ViewModelProvider.Factory {

    private final AppContainer appContainer;

    public SettingsViewModelFactory(AppContainer appContainer) {
        this.appContainer = appContainer;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(SettingsViewModel.class)) {
            return (T) new SettingsViewModel(
                    appContainer.authenticateUseCase(),
                    appContainer.updateTelemetryConsentUseCase(),
                    appContainer.userRepository(),
                    appContainer.syncClient(),
                    appContainer.executors(),
                    appContainer.appSettings()
            );
        }
        throw new IllegalArgumentException("Unknown ViewModel: " + modelClass.getName());
    }
}
