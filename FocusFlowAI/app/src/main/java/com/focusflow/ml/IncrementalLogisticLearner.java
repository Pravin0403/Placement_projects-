package com.focusflow.ml;

/**
 * Online logistic regression that starts from the global baseline and updates
 * from "was this suggestion helpful?" labels. One labeled window per feedback.
 */
public class IncrementalLogisticLearner {

    public static final float LEARNING_RATE = 0.05f;
    public static final float WEIGHT_CLIP = 2.5f;

    private final float[] weights;
    private float bias;
    private float[] lastScaled;
    private float lastProbability;

    public IncrementalLogisticLearner(float[] weights, float bias, float[] lastScaled, float lastProbability) {
        this.weights = copyOrBaseline(weights);
        this.bias = Float.isNaN(bias) ? LogisticPredictor.BASE_BIAS : bias;
        this.lastScaled = lastScaled == null ? null : lastScaled.clone();
        this.lastProbability = lastProbability;
    }

    public static IncrementalLogisticLearner baseline() {
        return new IncrementalLogisticLearner(
                LogisticPredictor.BASE_WEIGHTS,
                LogisticPredictor.BASE_BIAS,
                null,
                0f
        );
    }

    public void remember(float[] scaledFeatures, float predictedProbability) {
        lastScaled = scaledFeatures == null ? null : scaledFeatures.clone();
        lastProbability = clamp01(predictedProbability);
    }

    /**
     * Helpful → the window really was high-risk (y=1).
     * Not helpful → false alarm (y=0).
     */
    public boolean learnFromFeedback(boolean helpful) {
        if (lastScaled == null || lastScaled.length != weights.length) {
            return false;
        }
        float y = helpful ? 1f : 0f;
        float error = lastProbability - y;
        for (int i = 0; i < weights.length; i++) {
            weights[i] = clip(weights[i] - LEARNING_RATE * error * lastScaled[i]);
        }
        bias = clip(bias - LEARNING_RATE * error);
        lastScaled = null;
        return true;
    }

    public float predictProbability(float[] scaledFeatures) {
        float logit = bias;
        int length = Math.min(weights.length, scaledFeatures.length);
        for (int i = 0; i < length; i++) {
            logit += weights[i] * scaledFeatures[i];
        }
        return (float) (1.0 / (1.0 + Math.exp(-logit)));
    }

    public float[] weights() {
        return weights.clone();
    }

    public float bias() {
        return bias;
    }

    public float[] lastScaled() {
        return lastScaled == null ? null : lastScaled.clone();
    }

    public float lastProbability() {
        return lastProbability;
    }

    private static float[] copyOrBaseline(float[] weights) {
        if (weights == null || weights.length != FeatureVector.SIZE) {
            return LogisticPredictor.BASE_WEIGHTS.clone();
        }
        return weights.clone();
    }

    private static float clip(float value) {
        return Math.max(-WEIGHT_CLIP, Math.min(WEIGHT_CLIP, value));
    }

    private static float clamp01(float value) {
        return Math.max(0f, Math.min(1f, value));
    }
}
