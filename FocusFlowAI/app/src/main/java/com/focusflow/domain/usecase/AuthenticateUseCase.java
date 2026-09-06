package com.focusflow.domain.usecase;

import com.focusflow.domain.repository.AuthRepository;

public class AuthenticateUseCase {

    private final AuthRepository authRepository;

    public AuthenticateUseCase(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    public void login(String email, String password) throws Exception {
        authRepository.login(email.trim(), password);
    }

    public void register(String email, String password) throws Exception {
        authRepository.register(email.trim(), password);
    }

    public void logout() {
        authRepository.logout();
    }

    public boolean isSignedIn() {
        return authRepository.isSignedIn();
    }
}
