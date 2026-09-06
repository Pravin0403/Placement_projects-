package com.focusflow.data.remote.interceptor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.focusflow.data.local.AppSettings;
import com.focusflow.data.remote.api.FocusFlowApi;
import com.focusflow.data.remote.dto.AuthResponse;
import com.focusflow.data.remote.dto.RefreshRequest;

import java.io.IOException;

import okhttp3.Authenticator;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;
import retrofit2.Call;

/**
 * Rotates JWT access tokens using a refresh client that is not authenticated.
 */
public class TokenAuthenticator implements Authenticator {

    private final AppSettings settings;
    private final FocusFlowApi refreshApi;
    private final Object lock = new Object();

    public TokenAuthenticator(AppSettings settings, FocusFlowApi refreshApi) {
        this.settings = settings;
        this.refreshApi = refreshApi;
    }

    @Nullable
    @Override
    public Request authenticate(@Nullable Route route, @NonNull Response response) throws IOException {
        if (response.request().url().encodedPath().contains("auth/refresh")) {
            return null;
        }
        if (responseCount(response) >= 2) {
            return null;
        }
        String refresh = settings.refreshToken();
        if (refresh == null || refresh.isEmpty()) {
            return null;
        }
        synchronized (lock) {
            String currentAccess = settings.accessToken();
            String failedAccess = bearer(response.request());
            if (currentAccess != null && !currentAccess.isEmpty() && !currentAccess.equals(failedAccess)) {
                return response.request().newBuilder()
                        .header("Authorization", "Bearer " + currentAccess)
                        .build();
            }
            try {
                Call<AuthResponse> call = refreshApi.refresh(new RefreshRequest(refresh));
                retrofit2.Response<AuthResponse> refreshed = call.execute();
                if (!refreshed.isSuccessful() || refreshed.body() == null
                        || refreshed.body().accessToken == null) {
                    settings.setTokens("", "");
                    return null;
                }
                settings.setTokens(refreshed.body().accessToken, refreshed.body().refreshToken);
                return response.request().newBuilder()
                        .header("Authorization", "Bearer " + refreshed.body().accessToken)
                        .build();
            } catch (Exception ignored) {
                return null;
            }
        }
    }

    @Nullable
    private static String bearer(@NonNull Request request) {
        String header = request.header("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            return "";
        }
        return header.substring("Bearer ".length());
    }

    private static int responseCount(@NonNull Response response) {
        int count = 1;
        Response prior = response.priorResponse();
        while (prior != null) {
            count++;
            prior = prior.priorResponse();
        }
        return count;
    }
}
