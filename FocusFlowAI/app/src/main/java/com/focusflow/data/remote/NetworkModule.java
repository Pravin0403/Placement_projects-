package com.focusflow.data.remote;

import com.focusflow.BuildConfig;
import com.focusflow.core.network.NetworkConstants;
import com.focusflow.data.local.AppSettings;
import com.focusflow.data.remote.api.FocusFlowApi;
import com.focusflow.data.remote.interceptor.AuthInterceptor;
import com.focusflow.data.remote.interceptor.TokenAuthenticator;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class NetworkModule {

    private NetworkModule() {
    }

    public static FocusFlowApi create(AppSettings settings) {
        String base = normalizeBase(settings);
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BASIC);
        OkHttpClient refreshClient = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .connectTimeout(NetworkConstants.CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(NetworkConstants.CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .build();
        FocusFlowApi refreshApi = new Retrofit.Builder()
                .baseUrl(base)
                .client(refreshClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(FocusFlowApi.class);
        OkHttpClient client = refreshClient.newBuilder()
                .addInterceptor(new AuthInterceptor(settings))
                .authenticator(new TokenAuthenticator(settings, refreshApi))
                .build();
        return new Retrofit.Builder()
                .baseUrl(base)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(FocusFlowApi.class);
    }

    private static String normalizeBase(AppSettings settings) {
        String base = settings.apiBaseUrl();
        if (base == null || base.isEmpty()) {
            base = BuildConfig.API_BASE_URL;
        }
        if (!base.endsWith("/")) {
            base = base + "/";
        }
        return base;
    }
}
