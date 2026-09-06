package com.focusflow.engine;

import com.focusflow.core.common.Constants;
import com.focusflow.ml.PredictionResult;

public class DecisionEngineImpl implements DecisionEngine {

    @Override
    public Decision decide(PredictionResult prediction) {
        return decide(prediction, Long.MAX_VALUE, Long.MAX_VALUE, Constants.DEFAULT_RISK_THRESHOLD);
    }

    public Decision decide(
            PredictionResult prediction,
            long sessionDurationMs,
            long millisSinceLastAlert,
            float personalThreshold
    ) {
        float risk = prediction.riskScore();
        if (sessionDurationMs < Constants.MIN_SESSION_BEFORE_ALERT_MS) {
            return Decision.NO_ACTION;
        }
        if (millisSinceLastAlert < Constants.ALERT_COOLDOWN_MS) {
            return Decision.NO_ACTION;
        }
        if (risk >= Math.max(0.88f, personalThreshold + 0.12f)) {
            return Decision.FOCUS_ALERT;
        }
        if (risk >= personalThreshold) {
            return Decision.SUGGEST_BREAK;
        }
        if (risk >= personalThreshold - 0.08f) {
            return Decision.FEEDBACK_REQUEST;
        }
        return Decision.NO_ACTION;
    }
}
