package com.focusflow.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.ServiceCompat;

import com.focusflow.FocusFlowApplication;
import com.focusflow.R;
import com.focusflow.core.common.AppExecutors;
import com.focusflow.core.common.Constants;
import com.focusflow.core.di.AppContainer;
import com.focusflow.core.util.NotificationHelper;
import com.focusflow.engine.FocusEngine;
import com.focusflow.feature.MainActivity;
import com.focusflow.telemetry.processor.TelemetryProcessor;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class FocusSessionService extends Service {

    public static final String ACTION_START = "com.focusflow.action.START_SESSION";
    public static final String ACTION_STOP = "com.focusflow.action.STOP_SESSION";

    private TelemetryProcessor telemetryProcessor;
    private FocusEngine focusEngine;
    private AppExecutors executors;
    private ScheduledFuture<?> predictionFuture;

    public static void start(Context context) {
        Intent intent = new Intent(context, FocusSessionService.class);
        intent.setAction(ACTION_START);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

    public static void stop(Context context) {
        Intent intent = new Intent(context, FocusSessionService.class);
        intent.setAction(ACTION_STOP);
        context.startService(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        NotificationHelper.ensureChannel(this);
        AppContainer container = ((FocusFlowApplication) getApplication()).getAppContainer();
        telemetryProcessor = container.telemetryProcessor();
        focusEngine = container.focusEngine();
        executors = container.executors();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent == null ? ACTION_START : intent.getAction();
        if (ACTION_STOP.equals(action)) {
            shutdown();
            stopForeground(STOP_FOREGROUND_REMOVE);
            stopSelf();
            return START_NOT_STICKY;
        }
        startInForeground();
        executors.diskIo().execute(() -> {
            containerStartSession();
            telemetryProcessor.startIfConsented();
        });
        if (predictionFuture == null || predictionFuture.isCancelled()) {
            predictionFuture = executors.diskIo().scheduleAtFixedRate(
                    () -> focusEngine.onWindowReady(),
                    Constants.PREDICTION_INTERVAL_MS,
                    Constants.PREDICTION_INTERVAL_MS,
                    TimeUnit.MILLISECONDS
            );
        }
        return START_STICKY;
    }

    private void containerStartSession() {
        ((FocusFlowApplication) getApplication()).getAppContainer().sessionRepository().startSession();
    }

    private void startInForeground() {
        PendingIntent content = PendingIntent.getActivity(
                this,
                0,
                new Intent(this, MainActivity.class),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        Notification notification = new NotificationCompat.Builder(this, Constants.NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(getString(R.string.session_notification_title))
                .setContentText(getString(R.string.session_notification_body))
                .setContentIntent(content)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .build();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ServiceCompat.startForeground(
                    this,
                    Constants.SESSION_NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            );
        } else {
            startForeground(Constants.SESSION_NOTIFICATION_ID, notification);
        }
    }

    private void shutdown() {
        if (predictionFuture != null) {
            predictionFuture.cancel(false);
            predictionFuture = null;
        }
        executors.diskIo().execute(() -> {
            focusEngine.onWindowReady();
            telemetryProcessor.stop();
            ((FocusFlowApplication) getApplication()).getAppContainer().sessionRepository().endSession();
        });
    }

    @Override
    public void onDestroy() {
        if (predictionFuture != null) {
            predictionFuture.cancel(false);
        }
        if (telemetryProcessor != null) {
            telemetryProcessor.stop();
        }
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
