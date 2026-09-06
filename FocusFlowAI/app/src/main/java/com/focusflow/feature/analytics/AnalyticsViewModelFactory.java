package com.focusflow.feature.analytics;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.focusflow.core.di.AppContainer;

public class AnalyticsViewModelFactory implements ViewModelProvider.Factory {

    private final AppContainer appContainer;

    public AnalyticsViewModelFactory(AppContainer appContainer) {
        this.appContainer = appContainer;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(AnalyticsViewModel.class)) {
            return (T) new AnalyticsViewModel(appContainer.predictionDao(), appContainer.modelRepository());
        }
        throw new IllegalArgumentException("Unknown ViewModel: " + modelClass.getName());
    }
}
