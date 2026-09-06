package com.focusflow.ml;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.SharedPreferences;

import org.tensorflow.lite.Interpreter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.net.HttpURLConnection;
import java.net.URL;

public class ModelManagerImpl implements ModelManager {

    public static final String ASSET_MODEL = "models/focus_risk.tflite";
    private static final String MODEL_DIRECTORY = "models";
    private static final String DOWNLOADED_MODEL = "focus_risk.tflite";
    private static final String MODEL_PREFERENCES = "focusflow_model";
    private static final String KEY_MODEL_VERSION = "active_model_version";
    private static final int MAX_MODEL_BYTES = 10 * 1024 * 1024;

    private final Context context;

    public ModelManagerImpl(Context context) {
        this.context = context.getApplicationContext();
    }

    @Override
    public String loadedVersion() {
        if (downloadedModelFile().isFile()) {
            return context.getSharedPreferences(MODEL_PREFERENCES, Context.MODE_PRIVATE)
                    .getString(KEY_MODEL_VERSION, "tflite-1.0.0");
        }
        return hasBundledTfliteModel() ? "tflite-1.0.0" : "logistic-baseline-1.0.0";
    }

    public boolean hasTfliteModel() {
        return downloadedModelFile().isFile() || hasBundledTfliteModel();
    }

    private boolean hasBundledTfliteModel() {
        try {
            String[] models = context.getAssets().list("models");
            if (models == null) {
                return false;
            }
            for (String name : models) {
                if ("focus_risk.tflite".equals(name)) {
                    return true;
                }
            }
            return false;
        } catch (IOException exception) {
            return false;
        }
    }

    public Interpreter openInterpreter() throws IOException {
        File downloaded = downloadedModelFile();
        if (downloaded.isFile()) {
            return openFileInterpreter(downloaded);
        }
        try (AssetFileDescriptor descriptor = context.getAssets().openFd(ASSET_MODEL);
             FileInputStream inputStream = new FileInputStream(descriptor.getFileDescriptor())) {
            FileChannel channel = inputStream.getChannel();
            MappedByteBuffer buffer = channel.map(
                    FileChannel.MapMode.READ_ONLY,
                    descriptor.getStartOffset(),
                    descriptor.getDeclaredLength()
            );
            return new Interpreter(buffer);
        }
    }

    /** Downloads, validates, and activates an HTTPS TFLite artifact without discarding the prior model on failure. */
    public boolean downloadAndActivate(String artifactUrl, String version) throws IOException {
        if (artifactUrl == null || artifactUrl.trim().isEmpty()) {
            return false;
        }
        URL url = new URL(artifactUrl);
        if (!"https".equalsIgnoreCase(url.getProtocol())) {
            throw new IOException("Model artifact must use HTTPS");
        }

        File directory = new File(context.getFilesDir(), MODEL_DIRECTORY);
        if (!directory.exists() && !directory.mkdirs()) {
            throw new IOException("Unable to create model directory");
        }
        File temporary = new File(directory, DOWNLOADED_MODEL + ".download");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(15_000);
        connection.setReadTimeout(30_000);
        connection.setInstanceFollowRedirects(false);
        try {
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException("Model download failed: " + connection.getResponseCode());
            }
            int declaredLength = connection.getContentLength();
            if (declaredLength > MAX_MODEL_BYTES) {
                throw new IOException("Model artifact exceeds size limit");
            }
            copyWithLimit(connection.getInputStream(), temporary);
            try (Interpreter ignored = openFileInterpreter(temporary)) {
                // Opening the interpreter validates the model before it replaces the active artifact.
            }
            activateDownloadedModel(temporary);
            context.getSharedPreferences(MODEL_PREFERENCES, Context.MODE_PRIVATE)
                    .edit()
                    .putString(KEY_MODEL_VERSION, version == null || version.isEmpty() ? "tflite-1.0.0" : version)
                    .apply();
            return true;
        } finally {
            connection.disconnect();
            if (temporary.exists()) {
                temporary.delete();
            }
        }
    }

    private File downloadedModelFile() {
        return new File(new File(context.getFilesDir(), MODEL_DIRECTORY), DOWNLOADED_MODEL);
    }

    private void activateDownloadedModel(File temporary) throws IOException {
        File active = downloadedModelFile();
        File backup = new File(active.getParentFile(), DOWNLOADED_MODEL + ".previous");
        if (backup.exists() && !backup.delete()) {
            throw new IOException("Unable to clear previous model backup");
        }
        boolean movedActive = active.exists();
        if (movedActive && !active.renameTo(backup)) {
            throw new IOException("Unable to preserve active model");
        }
        if (!temporary.renameTo(active)) {
            if (movedActive) {
                backup.renameTo(active);
            }
            throw new IOException("Unable to activate downloaded model");
        }
        if (backup.exists() && !backup.delete()) {
            // The new model is active; a stale backup can safely be removed on the next update.
        }
    }

    private static Interpreter openFileInterpreter(File file) throws IOException {
        try (FileInputStream inputStream = new FileInputStream(file)) {
            FileChannel channel = inputStream.getChannel();
            MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
            return new Interpreter(buffer);
        }
    }

    private static void copyWithLimit(InputStream input, File destination) throws IOException {
        byte[] buffer = new byte[8192];
        int total = 0;
        try (InputStream source = input; FileOutputStream output = new FileOutputStream(destination)) {
            int read;
            while ((read = source.read(buffer)) != -1) {
                total += read;
                if (total > MAX_MODEL_BYTES) {
                    throw new IOException("Model artifact exceeds size limit");
                }
                output.write(buffer, 0, read);
            }
            output.flush();
        }
    }
}
