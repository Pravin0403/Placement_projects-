package com.focusflow.feature.session;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.focusflow.core.di.AppContainer;

public class SessionViewModelFactory implements ViewModelProvider.Factory {

    private final AppContainer appContainer;

    public SessionViewModelFactory(AppContainer appContainer) {
        this.appContainer = appContainer;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(SessionViewModel.class)) {
            return (T) new SessionViewModel(appContainer.sessionRepository());
        }
        throw new IllegalArgumentException("Unknown ViewModel: " + modelClass.getName());
    }
}
