package com.focusflow.data.remote;

import com.focusflow.core.network.NetworkConstants;
import com.focusflow.data.local.AppSettings;
import com.focusflow.data.local.dao.FeedbackDao;
import com.focusflow.data.local.dao.PredictionDao;
import com.focusflow.data.local.dao.TelemetryEventDao;
import com.focusflow.data.local.entity.FeedbackEntity;
import com.focusflow.data.local.entity.PredictionEntity;
import com.focusflow.data.local.entity.TelemetryEventEntity;
import com.focusflow.data.remote.api.FocusFlowApi;
import com.focusflow.data.remote.dto.FeedbackDto;
import com.focusflow.data.remote.dto.PredictionBatchRequest;
import com.focusflow.data.remote.dto.PredictionDto;
import com.focusflow.data.remote.dto.TelemetryBatchRequest;
import com.focusflow.data.remote.dto.TelemetryEventDto;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Response;

public class SyncClient {

    private final FocusFlowApi api;
    private final TelemetryEventDao eventDao;
    private final PredictionDao predictionDao;
    private final FeedbackDao feedbackDao;
    private final AppSettings settings;

    public SyncClient(
            FocusFlowApi api,
            TelemetryEventDao eventDao,
            PredictionDao predictionDao,
            FeedbackDao feedbackDao,
            AppSettings settings
    ) {
        this.api = api;
        this.eventDao = eventDao;
        this.predictionDao = predictionDao;
        this.feedbackDao = feedbackDao;
        this.settings = settings;
    }

    public boolean sync() throws Exception {
        if (settings.isCircuitOpen()) {
            return false;
        }
        if (settings.accessToken() == null || settings.accessToken().isEmpty()) {
            return true;
        }
        uploadEvents();
        uploadPredictions();
        uploadFeedback();
        settings.recordSyncSuccess();
        return true;
    }

    private void uploadEvents() throws Exception {
        List<TelemetryEventEntity> unsynced = eventDao.unsynced(NetworkConstants.BATCH_SIZE);
        if (unsynced.isEmpty()) {
            return;
        }
        List<TelemetryEventDto> dtos = new ArrayList<>(unsynced.size());
        List<String> ids = new ArrayList<>(unsynced.size());
        for (TelemetryEventEntity entity : unsynced) {
            TelemetryEventDto dto = new TelemetryEventDto();
            dto.eventId = entity.getEventId();
            dto.sessionId = String.valueOf(entity.getSessionId());
            dto.type = entity.getType();
            dto.packageName = entity.getPackageName();
            dto.timestamp = entity.getTimestampEpochMs();
            dtos.add(dto);
            ids.add(entity.getEventId());
        }
        Response<Void> response = api.uploadTelemetry(ids.get(0), new TelemetryBatchRequest(dtos)).execute();
        if (!response.isSuccessful()) {
            throw new IllegalStateException("Telemetry sync failed: " + response.code());
        }
        eventDao.markSynced(ids);
    }

    private void uploadPredictions() throws Exception {
        List<PredictionEntity> unsynced = predictionDao.unsynced(NetworkConstants.BATCH_SIZE);
        if (unsynced.isEmpty()) {
            return;
        }
        List<PredictionDto> dtos = new ArrayList<>(unsynced.size());
        List<String> ids = new ArrayList<>(unsynced.size());
        for (PredictionEntity entity : unsynced) {
            PredictionDto dto = new PredictionDto();
            dto.predictionId = entity.getPredictionId();
            dto.sessionId = String.valueOf(entity.getSessionId());
            dto.riskScore = entity.getRiskScore();
            dto.modelVersion = entity.getModelVersion();
            dto.createdAt = entity.getCreatedAtEpochMs();
            dtos.add(dto);
            ids.add(entity.getPredictionId());
        }
        Response<Void> response = api.uploadPredictions(ids.get(0), new PredictionBatchRequest(dtos)).execute();
        if (!response.isSuccessful()) {
            throw new IllegalStateException("Prediction sync failed: " + response.code());
        }
        predictionDao.markSynced(ids);
    }

    private void uploadFeedback() throws Exception {
        List<FeedbackEntity> unsynced = feedbackDao.unsynced(NetworkConstants.BATCH_SIZE);
        List<Long> syncedIds = new ArrayList<>(unsynced.size());
        for (FeedbackEntity entity : unsynced) {
            FeedbackDto dto = new FeedbackDto();
            dto.predictionId = entity.getPredictionId();
            dto.helpful = entity.isHelpful();
            dto.actionTaken = entity.getActionTaken();
            dto.createdAt = entity.getCreatedAtEpochMs();
            Response<Void> response = api.uploadFeedback(dto).execute();
            if (!response.isSuccessful() && response.code() != 409) {
                throw new IllegalStateException("Feedback sync failed: " + response.code());
            }
            syncedIds.add(entity.getId());
        }
        if (!syncedIds.isEmpty()) {
            feedbackDao.markSynced(syncedIds);
        }
    }
}
