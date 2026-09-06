package com.focusflow.feature.onboarding;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.focusflow.core.common.AppExecutors;
import com.focusflow.domain.model.OnboardingDraft;
import com.focusflow.domain.usecase.CompleteOnboardingUseCase;

public class OnboardingViewModel extends ViewModel {

    private final CompleteOnboardingUseCase completeOnboardingUseCase;
    private final AppExecutors executors;

    private final MutableLiveData<Boolean> canContinue = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> completed = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    private String displayName = "";
    private boolean privacyAcknowledged;
    private boolean telemetryConsented;
    private boolean submitting;

    public OnboardingViewModel(
            CompleteOnboardingUseCase completeOnboardingUseCase,
            AppExecutors executors
    ) {
        this.completeOnboardingUseCase = completeOnboardingUseCase;
        this.executors = executors;
    }

    public LiveData<Boolean> canContinue() {
        return canContinue;
    }

    public LiveData<Boolean> completed() {
        return completed;
    }

    public LiveData<String> errorMessage() {
        return errorMessage;
    }

    public void onDisplayNameChanged(@NonNull String value) {
        displayName = value;
    }

    public void onPrivacyAcknowledged(boolean acknowledged) {
        privacyAcknowledged = acknowledged;
        canContinue.setValue(privacyAcknowledged && !submitting);
    }

    public void onTelemetryConsented(boolean consented) {
        telemetryConsented = consented;
    }

    public void completeOnboarding() {
        if (!privacyAcknowledged || submitting) {
            return;
        }
        submitting = true;
        canContinue.setValue(false);
        OnboardingDraft draft = new OnboardingDraft(displayName, true, telemetryConsented);
        executors.diskIo().execute(() -> {
            try {
                completeOnboardingUseCase.execute(draft);
                completed.postValue(true);
            } catch (RuntimeException exception) {
                submitting = false;
                errorMessage.postValue(exception.getMessage());
                canContinue.postValue(true);
            }
        });
    }
}
