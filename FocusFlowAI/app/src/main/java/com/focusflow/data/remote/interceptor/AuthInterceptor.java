package com.focusflow.data.remote.interceptor;

import androidx.annotation.NonNull;

import com.focusflow.data.local.AppSettings;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {

    private final AppSettings settings;

    public AuthInterceptor(AppSettings settings) {
        this.settings = settings;
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request original = chain.request();
        String token = settings.accessToken();
        if (token == null || token.isEmpty()) {
            return chain.proceed(original);
        }
        Request authed = original.newBuilder()
                .header("Authorization", "Bearer " + token)
                .build();
        return chain.proceed(authed);
    }
}
