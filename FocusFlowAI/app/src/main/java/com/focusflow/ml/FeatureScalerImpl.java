package com.focusflow.ml;

/**
 * Z-score scaler fitted on the synthetic training set in ml/train_baseline.py.
 */
public class FeatureScalerImpl implements FeatureScaler {

    private static final float[] MEAN = {
            4.2f, 1.8f, 28.0f, 28.0f, 0.35f, 4.0f, 14.5f, 4.0f, 1.1f, 0.32f
    };
    private static final float[] STD = {
            3.4f, 1.6f, 18.0f, 18.0f, 0.28f, 5.0f, 5.2f, 2.0f, 1.4f, 0.22f
    };

    @Override
    public float[] scale(float[] rawFeatures) {
        float[] scaled = new float[rawFeatures.length];
        for (int i = 0; i < rawFeatures.length; i++) {
            float std = STD[i] == 0f ? 1f : STD[i];
            scaled[i] = (rawFeatures[i] - MEAN[i]) / std;
        }
        return scaled;
    }
}
