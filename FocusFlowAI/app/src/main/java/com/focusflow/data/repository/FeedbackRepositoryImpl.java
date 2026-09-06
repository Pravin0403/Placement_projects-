package com.focusflow.data.repository;

import com.focusflow.data.local.dao.FeedbackDao;
import com.focusflow.data.local.entity.FeedbackEntity;
import com.focusflow.domain.repository.FeedbackRepository;

public class FeedbackRepositoryImpl implements FeedbackRepository {

    private final FeedbackDao feedbackDao;

    public FeedbackRepositoryImpl(FeedbackDao feedbackDao) {
        this.feedbackDao = feedbackDao;
    }

    @Override
    public void submit(String predictionId, boolean helpful, String actionTaken) {
        feedbackDao.insert(new FeedbackEntity(
                0,
                predictionId,
                helpful,
                actionTaken,
                System.currentTimeMillis(),
                false
        ));
    }
}
