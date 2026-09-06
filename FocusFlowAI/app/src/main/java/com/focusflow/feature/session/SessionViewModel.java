package com.focusflow.feature.session;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.focusflow.domain.model.FocusSession;
import com.focusflow.domain.repository.SessionRepository;

public class SessionViewModel extends ViewModel {

    private final LiveData<FocusSession> activeSession;

    public SessionViewModel(SessionRepository sessionRepository) {
        activeSession = sessionRepository.observeActive();
    }

    public LiveData<FocusSession> activeSession() {
        return activeSession;
    }
}
