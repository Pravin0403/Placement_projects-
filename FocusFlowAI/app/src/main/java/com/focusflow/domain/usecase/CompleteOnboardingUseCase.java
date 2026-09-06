package com.focusflow.domain.usecase;

import com.focusflow.domain.model.OnboardingDraft;
import com.focusflow.domain.repository.UserRepository;

public class CompleteOnboardingUseCase {

    private final UserRepository userRepository;

    public CompleteOnboardingUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void execute(OnboardingDraft draft) {
        if (!draft.canContinue()) {
            throw new IllegalStateException("Privacy acknowledgement is required");
        }
        userRepository.completeOnboarding(draft);
    }
}
