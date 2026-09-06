package com.focusflow.domain.usecase;

import com.focusflow.domain.repository.SessionRepository;

public class StartFocusSessionUseCase {

    private final SessionRepository sessionRepository;

    public StartFocusSessionUseCase(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    public long execute() {
        return sessionRepository.startSession();
    }
}
