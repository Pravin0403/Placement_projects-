package com.focusflow.feature.settings;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.focusflow.core.common.AppExecutors;
import com.focusflow.data.local.AppSettings;
import com.focusflow.data.remote.SyncClient;
import com.focusflow.domain.model.UserPreferences;
import com.focusflow.domain.repository.UserRepository;
import com.focusflow.domain.usecase.AuthenticateUseCase;
import com.focusflow.domain.usecase.UpdateTelemetryConsentUseCase;

public class SettingsViewModel extends ViewModel {

    private final AuthenticateUseCase authenticateUseCase;
    private final UpdateTelemetryConsentUseCase consentUseCase;
    private final UserRepository userRepository;
    private final SyncClient syncClient;
    private final AppExecutors executors;
    private final AppSettings appSettings;
    private final MutableLiveData<Boolean> signedIn = new MutableLiveData<>(false);
    private final MutableLiveData<String> message = new MutableLiveData<>();
    private final MutableLiveData<Integer> thresholdPercent = new MutableLiveData<>();

    public SettingsViewModel(
            AuthenticateUseCase authenticateUseCase,
            UpdateTelemetryConsentUseCase consentUseCase,
            UserRepository userRepository,
            SyncClient syncClient,
            AppExecutors executors,
            AppSettings appSettings
    ) {
        this.authenticateUseCase = authenticateUseCase;
        this.consentUseCase = consentUseCase;
        this.userRepository = userRepository;
        this.syncClient = syncClient;
        this.executors = executors;
        this.appSettings = appSettings;
        thresholdPercent.setValue(Math.round(appSettings.personalRiskThreshold() * 100f));
    }

    public LiveData<UserPreferences> preferences() {
        return userRepository.observePreferences();
    }

    public LiveData<Boolean> signedIn() {
        return signedIn;
    }

    public LiveData<String> message() {
        return message;
    }

    public LiveData<Integer> thresholdPercent() {
        return thresholdPercent;
    }

    public void refreshAuthState() {
        signedIn.setValue(authenticateUseCase.isSignedIn());
        thresholdPercent.setValue(Math.round(appSettings.personalRiskThreshold() * 100f));
    }

    public void login(String email, String password) {
        submitAuth(true, email, password);
    }

    public void register(String email, String password) {
        submitAuth(false, email, password);
    }

    public void logout() {
        authenticateUseCase.logout();
        refreshAuthState();
    }

    public void setTelemetryConsent(boolean consented) {
        executors.diskIo().execute(() -> consentUseCase.execute(consented));
    }

    public void syncNow() {
        executors.diskIo().execute(() -> {
            try {
                boolean ok = syncClient.sync();
                executors.mainThread().execute(() ->
                        message.setValue(ok ? "Sync finished" : "Sync skipped (offline circuit)"));
            } catch (Exception exception) {
                executors.mainThread().execute(() -> message.setValue("Sync failed. It will retry automatically."));
            }
        });
    }

    private void submitAuth(boolean login, String email, String password) {
        if (email == null || email.trim().isEmpty() || password == null || password.length() < 8) {
            message.setValue("Enter an email and a password of at least 8 characters.");
            return;
        }
        executors.diskIo().execute(() -> {
            try {
                if (login) {
                    authenticateUseCase.login(email, password);
                } else {
                    authenticateUseCase.register(email, password);
                }
                executors.mainThread().execute(() -> {
                    signedIn.setValue(true);
                    message.setValue("Account ready");
                });
            } catch (Exception exception) {
                executors.mainThread().execute(() ->
                        message.setValue("Could not reach the server. Check the API and try again."));
            }
        });
    }
}
