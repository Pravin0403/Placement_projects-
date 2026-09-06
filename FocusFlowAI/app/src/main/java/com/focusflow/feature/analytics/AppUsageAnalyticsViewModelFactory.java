package com.focusflow.feature.analytics;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.focusflow.core.di.AppContainer;

/**
 * Factory for creating AppUsageAnalyticsViewModel instances.
 */
public class AppUsageAnalyticsViewModelFactory implements ViewModelProvider.Factory {

    private final AppContainer appContainer;

    public AppUsageAnalyticsViewModelFactory(AppContainer appContainer) {
        this.appContainer = appContainer;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(AppUsageAnalyticsViewModel.class)) {
            return (T) new AppUsageAnalyticsViewModel(
                    appContainer.appUsageRepository(),
                    appContainer.executors()
            );
        }
        throw new IllegalArgumentException("Unknown ViewModel: " + modelClass.getName());
    }
}