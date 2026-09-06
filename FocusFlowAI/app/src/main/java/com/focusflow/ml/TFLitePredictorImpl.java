package com.focusflow.ml;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.tensorflow.lite.Interpreter;

public class TFLitePredictorImpl implements TFLitePredictor {

    private final ModelManagerImpl modelManager;
    private final FeatureScaler scaler;
    private final LogisticPredictor logisticPredictor;
    private final PredictionPostProcessor postProcessor;

    public TFLitePredictorImpl(
            ModelManagerImpl modelManager,
            FeatureScaler scaler,
            LogisticPredictor logisticPredictor,
            PredictionPostProcessor postProcessor
    ) {
        this.modelManager = modelManager;
        this.scaler = scaler;
        this.logisticPredictor = logisticPredictor;
        this.postProcessor = postProcessor;
    }

    @Override
    public PredictionResult predict(float[] features) {
        long started = System.nanoTime();
        float[] scaled = scaler.scale(features);
        float probability;
        String version = modelManager.loadedVersion();
        if (modelManager.hasTfliteModel()) {
            probability = inferTflite(scaled);
        } else {
            probability = logisticPredictor.predictProbability(scaled);
        }
        long elapsedMs = (System.nanoTime() - started) / 1_000_000L;
        return postProcessor.process(probability, version, elapsedMs);
    }

    private float inferTflite(float[] scaled) {
        Interpreter interpreter = null;
        try {
            interpreter = modelManager.openInterpreter();
            ByteBuffer input = ByteBuffer.allocateDirect(FeatureVector.SIZE * 4);
            input.order(ByteOrder.nativeOrder());
            for (float value : scaled) {
                input.putFloat(value);
            }
            input.rewind();
            float[][] output = new float[1][1];
            interpreter.run(input, output);
            return output[0][0];
        } catch (IOException | RuntimeException exception) {
            return logisticPredictor.predictProbability(scaled);
        } finally {
            if (interpreter != null) {
                interpreter.close();
            }
        }
    }
}
