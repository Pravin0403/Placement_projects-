package com.focusflow.feature.focuszone;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.focusflow.core.di.AppContainer;

/**
 * Factory for creating FocusZoneViewModel instances.
 */
public class FocusZoneViewModelFactory implements ViewModelProvider.Factory {

    private final AppContainer appContainer;

    public FocusZoneViewModelFactory(AppContainer appContainer) {
        this.appContainer = appContainer;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(FocusZoneViewModel.class)) {
            return (T) new FocusZoneViewModel(
                    appContainer.focusZoneEngine(),
                    appContainer.executors()
            );
        }
        throw new IllegalArgumentException("Unknown ViewModel: " + modelClass.getName());
    }
}