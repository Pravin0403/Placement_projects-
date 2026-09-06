package com.focusflow.telemetry.collectors;

import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;

import com.focusflow.core.common.AppExecutors;
import com.focusflow.core.common.Constants;
import com.focusflow.telemetry.store.TelemetryEventStore;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class AppSwitchCollectorImpl implements AppSwitchCollector {

    private final Context context;
    private final TelemetryEventStore store;
    private final AppExecutors executors;

    private volatile boolean running;
    private long lastQueryEpochMs;
    private ScheduledFuture<?> pollFuture;

    public AppSwitchCollectorImpl(Context context, TelemetryEventStore store, AppExecutors executors) {
        this.context = context.getApplicationContext();
        this.store = store;
        this.executors = executors;
    }

    @Override
    public void start() {
        running = true;
        lastQueryEpochMs = System.currentTimeMillis();
        pollFuture = executors.diskIo().scheduleAtFixedRate(this::poll, 5, 15, TimeUnit.SECONDS);
    }

    @Override
    public void stop() {
        running = false;
        if (pollFuture != null) {
            pollFuture.cancel(false);
            pollFuture = null;
        }
    }

    private void poll() {
        if (!running) {
            return;
        }
        UsageStatsManager manager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        if (manager == null) {
            return;
        }
        long now = System.currentTimeMillis();
        UsageEvents events;
        try {
            events = manager.queryEvents(lastQueryEpochMs, now);
        } catch (RuntimeException ignored) {
            return;
        }
        if (events == null) {
            return;
        }
        UsageEvents.Event event = new UsageEvents.Event();
        String lastPackage = null;
        while (events.hasNextEvent()) {
            events.getNextEvent(event);
            if (event.getEventType() != UsageEvents.Event.MOVE_TO_FOREGROUND) {
                continue;
            }
            String packageName = event.getPackageName();
            if (packageName == null || packageName.equals(context.getPackageName())) {
                continue;
            }
            if (packageName.equals(lastPackage)) {
                continue;
            }
            lastPackage = packageName;
            store.record(Constants.EVENT_APP_SWITCH, packageName);
        }
        lastQueryEpochMs = now;
    }
}
