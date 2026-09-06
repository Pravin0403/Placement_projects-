package com.focusflow.ml;

/**
 * Interpretable logistic baseline used when a TFLite artifact is not bundled.
 */
public class LogisticPredictor {

    public static final float BASE_BIAS = -0.15f;

    public static final float[] BASE_WEIGHTS = {
            0.55f, 0.48f, 0.12f, 0.18f, 0.42f,
            -0.22f, 0.16f, 0.05f, 0.36f, 0.40f
    };

    public float predictProbability(float[] scaledFeatures) {
        if (scaledFeatures == null) {
            return 0.5f;
        }

        float logit = BASE_BIAS;
        int length = Math.min(BASE_WEIGHTS.length, scaledFeatures.length);

        for (int i = 0; i < length; i++) {
            logit += BASE_WEIGHTS[i] * scaledFeatures[i];
        }

        return (float) (1.0 / (1.0 + Math.exp(-logit)));
    }
}