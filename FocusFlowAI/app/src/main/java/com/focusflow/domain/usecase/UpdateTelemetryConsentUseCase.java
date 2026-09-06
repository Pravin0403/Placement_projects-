package com.focusflow.domain.usecase;

import com.focusflow.domain.repository.UserRepository;

public class UpdateTelemetryConsentUseCase {

    private final UserRepository userRepository;

    public UpdateTelemetryConsentUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void execute(boolean consented) {
        userRepository.setTelemetryConsented(consented);
    }
}
