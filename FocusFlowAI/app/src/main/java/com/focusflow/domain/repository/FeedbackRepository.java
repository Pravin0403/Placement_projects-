package com.focusflow.domain.repository;

public interface FeedbackRepository {

    void submit(String predictionId, boolean helpful, String actionTaken);
}
