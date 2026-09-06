package com.focusflow.domain.usecase;

import com.focusflow.domain.repository.SessionRepository;

public class EndFocusSessionUseCase {

    private final SessionRepository sessionRepository;

    public EndFocusSessionUseCase(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    public void execute() {
        sessionRepository.endSession();
    }
}
