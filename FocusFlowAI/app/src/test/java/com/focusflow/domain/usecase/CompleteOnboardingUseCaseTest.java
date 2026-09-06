package com.focusflow.domain.usecase;

import com.focusflow.domain.model.OnboardingDraft;
import com.focusflow.domain.model.UserPreferences;
import com.focusflow.domain.repository.UserRepository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class CompleteOnboardingUseCaseTest {

    @Test
    public void execute_persistsDraftWhenPrivacyAcknowledged() {
        FakeUserRepository repository = new FakeUserRepository();
        CompleteOnboardingUseCase useCase = new CompleteOnboardingUseCase(repository);

        useCase.execute(new OnboardingDraft("Alex", true, true));

        assertTrue(repository.saved);
    }

    @Test(expected = IllegalStateException.class)
    public void execute_rejectsMissingPrivacyAck() {
        CompleteOnboardingUseCase useCase = new CompleteOnboardingUseCase(new FakeUserRepository());
        useCase.execute(new OnboardingDraft("Alex", false, true));
    }

    private static final class FakeUserRepository implements UserRepository {
        boolean saved;

        @Override
        public LiveData<UserPreferences> observePreferences() {
            return new MutableLiveData<>(UserPreferences.empty());
        }

        @Override
        public void completeOnboarding(OnboardingDraft draft) {
            saved = true;
        }

        @Override
        public void setTelemetryConsented(boolean consented) {
        }
    }
}
