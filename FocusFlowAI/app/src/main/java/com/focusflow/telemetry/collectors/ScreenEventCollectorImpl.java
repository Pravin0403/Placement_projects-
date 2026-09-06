package com.focusflow.telemetry.collectors;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.focusflow.core.common.AppExecutors;
import com.focusflow.core.common.Constants;
import com.focusflow.telemetry.store.TelemetryEventStore;

public class ScreenEventCollectorImpl implements ScreenEventCollector {

    private final Context context;
    private final TelemetryEventStore store;
    private final AppExecutors executors;
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null || intent.getAction() == null) {
                return;
            }
            String type;
            if (Intent.ACTION_USER_PRESENT.equals(intent.getAction())
                    || Intent.ACTION_SCREEN_ON.equals(intent.getAction())) {
                type = Constants.EVENT_SCREEN_UNLOCK;
            } else if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
                type = Constants.EVENT_SCREEN_OFF;
            } else {
                return;
            }
            String eventType = type;
            executors.diskIo().execute(() -> store.record(eventType, null));
        }
    };

    private boolean registered;

    public ScreenEventCollectorImpl(Context context, TelemetryEventStore store, AppExecutors executors) {
        this.context = context.getApplicationContext();
        this.store = store;
        this.executors = executors;
    }

    @Override
    public void start() {
        if (registered) {
            return;
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_USER_PRESENT);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        context.registerReceiver(receiver, filter);
        registered = true;
    }

    @Override
    public void stop() {
        if (!registered) {
            return;
        }
        try {
            context.unregisterReceiver(receiver);
        } catch (IllegalArgumentException ignored) {
        }
        registered = false;
    }
}
