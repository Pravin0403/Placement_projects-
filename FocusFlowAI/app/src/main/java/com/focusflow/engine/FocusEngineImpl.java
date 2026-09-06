package com.focusflow.engine;

import com.focusflow.R;

import android.app.NotificationManager;
import android.content.Context;

import androidx.core.app.NotificationCompat;


import com.focusflow.core.common.Constants;
import com.focusflow.data.local.AppSettings;
import com.focusflow.data.local.dao.FocusSessionDao;
import com.focusflow.data.local.dao.FocusSnapshotDao;
import com.focusflow.data.local.dao.PredictionDao;
import com.focusflow.data.local.entity.FocusSessionEntity;
import com.focusflow.data.local.entity.FocusSnapshotEntity;
import com.focusflow.data.local.entity.PredictionEntity;
import com.focusflow.ml.FeatureVector;
import com.focusflow.ml.PredictionPostProcessorImpl;
import com.focusflow.ml.PredictionResult;
import com.focusflow.ml.SignalExplainer;
import com.focusflow.ml.TFLitePredictor;
import com.focusflow.telemetry.aggregator.SignalAggregatorImpl;

import java.util.List;
import java.util.UUID;

public class FocusEngineImpl implements FocusEngine {

    private final Context context;
    private final FocusSessionDao sessionDao;
    private final FocusSnapshotDao snapshotDao;
    private final PredictionDao predictionDao;
    private final SignalAggregatorImpl aggregator;
    private final TFLitePredictor predictor;
    private final RiskCalculator riskCalculator;
    private final DecisionEngineImpl decisionEngine;
    private final RecommendationEngine recommendationEngine;
    private final PredictionPostProcessorImpl postProcessor;
    private final AppSettings settings;

    public FocusEngineImpl(
            Context context,
            FocusSessionDao sessionDao,
            FocusSnapshotDao snapshotDao,
            PredictionDao predictionDao,
            SignalAggregatorImpl aggregator,
            TFLitePredictor predictor,
            RiskCalculator riskCalculator,
            DecisionEngineImpl decisionEngine,
            RecommendationEngine recommendationEngine,
            PredictionPostProcessorImpl postProcessor,
            AppSettings settings
    ) {
        this.context = context.getApplicationContext();
        this.sessionDao = sessionDao;
        this.snapshotDao = snapshotDao;
        this.predictionDao = predictionDao;
        this.aggregator = aggregator;
        this.predictor = predictor;
        this.riskCalculator = riskCalculator;
        this.decisionEngine = decisionEngine;
        this.recommendationEngine = recommendationEngine;
        this.postProcessor = postProcessor;
        this.settings = settings;
    }

    @Override
    public void onWindowReady() {
        FocusSessionEntity session = sessionDao.getActive();
        if (session == null) {
            return;
        }
        FeatureVector vector = aggregator.aggregate(settings.lastBreakDurationMs(), settings.previousWindowRisk());
        if (vector == null) {
            return;
        }
        PredictionResult raw = predictor.predict(vector.values());
        float personalRisk = riskCalculator.personalize(raw.riskScore(), settings.personalRiskThreshold());
        long now = System.currentTimeMillis();
        long sessionDuration = now - session.getStartedAtEpochMs();
        long sinceAlert = settings.lastAlertEpochMs() == 0L ? Long.MAX_VALUE : now - settings.lastAlertEpochMs();
        Decision decision = decisionEngine.decide(
                new PredictionResult(personalRisk, raw.modelVersion(), raw.inferenceTimeMs()),
                sessionDuration,
                sinceAlert,
                settings.personalRiskThreshold()
        );
        int focusScore = postProcessor.focusScore(personalRisk);
        List<String> signals = SignalExplainer.explain(vector);
        String recommendation = recommendationEngine.recommendationFor(decision);
        String predictionId = UUID.randomUUID().toString();

        predictionDao.insert(new PredictionEntity(
                0,
                predictionId,
                session.getId(),
                personalRisk,
                focusScore,
                raw.modelVersion(),
                raw.inferenceTimeMs(),
                now,
                join(signals),
                recommendation,
                false
        ));
        snapshotDao.insert(new FocusSnapshotEntity(
                0,
                focusScore,
                Math.round(personalRisk * 100f),
                session.getStartedAtEpochMs(),
                null,
                join(signals),
                recommendation,
                now
        ));
        settings.setPreviousWindowRisk(personalRisk);
        settings.setLastPredictionId(predictionId);

        if (decision == Decision.SUGGEST_BREAK || decision == Decision.FOCUS_ALERT) {
            settings.setLastAlertEpochMs(now);
            notifyUser(recommendation);
        }
    }

    private void notifyUser(String recommendation) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) {
            return;
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, Constants.NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Focus Session Alert")
                .setContentText(recommendation)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(recommendation))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);
        manager.notify(Constants.SESSION_NOTIFICATION_ID + 1, builder.build());
    }

    private static String join(List<String> signals) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < signals.size(); i++) {
            if (i > 0) {
                builder.append(',');
            }
            builder.append(signals.get(i));
        }
        return builder.toString();
    }
}
