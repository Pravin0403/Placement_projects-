package com.focusflow.ml;

/**
 * TensorFlow Lite inference. Must run off the main thread.
 */
public interface TFLitePredictor {

    PredictionResult predict(float[] features);
}
