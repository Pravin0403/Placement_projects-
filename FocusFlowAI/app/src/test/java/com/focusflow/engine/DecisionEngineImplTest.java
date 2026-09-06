package com.focusflow.engine;

import com.focusflow.core.common.Constants;
import com.focusflow.ml.PredictionResult;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DecisionEngineImplTest {

    private final DecisionEngineImpl engine = new DecisionEngineImpl();

    @Test
    public void decide_suggestsBreakWhenRiskCrossesThresholdAfterWarmup() {
        PredictionResult prediction = new PredictionResult(0.80f, "test", 4L);
        Decision decision = engine.decide(
                prediction,
                Constants.MIN_SESSION_BEFORE_ALERT_MS + 1,
                Constants.ALERT_COOLDOWN_MS + 1,
                Constants.DEFAULT_RISK_THRESHOLD
        );
        assertEquals(Decision.SUGGEST_BREAK, decision);
    }

    @Test
    public void decide_staysQuietDuringCooldown() {
        PredictionResult prediction = new PredictionResult(0.99f, "test", 4L);
        Decision decision = engine.decide(
                prediction,
                Constants.MIN_SESSION_BEFORE_ALERT_MS + 1,
                1_000L,
                Constants.DEFAULT_RISK_THRESHOLD
        );
        assertEquals(Decision.NO_ACTION, decision);
    }
}
