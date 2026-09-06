package com.focusflow.domain.usecase;

import com.focusflow.data.local.AppSettings;
import com.focusflow.domain.repository.FeedbackRepository;

public class SubmitFeedbackUseCase {

    private final FeedbackRepository feedbackRepository;
    private final AppSettings appSettings;

    public SubmitFeedbackUseCase(FeedbackRepository feedbackRepository, AppSettings appSettings) {
        this.feedbackRepository = feedbackRepository;
        this.appSettings = appSettings;
    }

    public void execute(boolean helpful, String actionTaken) {
        String predictionId = appSettings.lastPredictionId();
        if (predictionId == null || predictionId.isEmpty()) {
            return;
        }
        feedbackRepository.submit(predictionId, helpful, actionTaken);
        appSettings.adaptThreshold(helpful);
    }
}
