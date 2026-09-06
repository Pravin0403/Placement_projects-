package com.focusflow.data.repository;

import com.focusflow.data.local.AppSettings;
import com.focusflow.data.remote.api.FocusFlowApi;
import com.focusflow.data.remote.dto.AuthRequest;
import com.focusflow.data.remote.dto.AuthResponse;
import com.focusflow.domain.repository.AuthRepository;

import retrofit2.Response;

public class AuthRepositoryImpl implements AuthRepository {

    private final FocusFlowApi api;
    private final AppSettings settings;

    public AuthRepositoryImpl(FocusFlowApi api, AppSettings settings) {
        this.api = api;
        this.settings = settings;
    }

    @Override
    public boolean isSignedIn() {
        String token = settings.accessToken();
        return token != null && !token.isEmpty();
    }

    @Override
    public void login(String email, String password) throws Exception {
        Response<AuthResponse> response = api.login(new AuthRequest(email, password)).execute();
        persist(response);
    }

    @Override
    public void register(String email, String password) throws Exception {
        Response<AuthResponse> response = api.register(new AuthRequest(email, password)).execute();
        persist(response);
    }

    @Override
    public void logout() {
        settings.setTokens("", "");
    }

    private void persist(Response<AuthResponse> response) throws Exception {
        if (!response.isSuccessful() || response.body() == null || response.body().accessToken == null) {
            throw new IllegalStateException("Auth failed: " + response.code());
        }
        if (!settings.canStoreTokens()) {
            throw new IllegalStateException("Secure storage is unavailable on this device");
        }
        settings.setTokens(response.body().accessToken, response.body().refreshToken);
    }
}
