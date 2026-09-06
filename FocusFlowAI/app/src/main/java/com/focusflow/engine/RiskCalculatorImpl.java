package com.focusflow.engine;

public class RiskCalculatorImpl implements RiskCalculator {

    @Override
    public float personalize(float globalRisk, float userThreshold) {
        float shift = (com.focusflow.core.common.Constants.DEFAULT_RISK_THRESHOLD - userThreshold) * 0.35f;
        float adjusted = globalRisk - shift;
        return Math.max(0f, Math.min(1f, adjusted));
    }
}
