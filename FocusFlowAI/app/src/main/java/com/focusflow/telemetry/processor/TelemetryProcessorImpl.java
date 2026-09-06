package com.focusflow.telemetry.processor;

import com.focusflow.data.local.dao.UserPreferencesDao;
import com.focusflow.data.local.entity.UserPreferencesEntity;
import com.focusflow.telemetry.collectors.AppSwitchCollector;
import com.focusflow.telemetry.collectors.ScreenEventCollector;

public class TelemetryProcessorImpl implements TelemetryProcessor {

    private final UserPreferencesDao preferencesDao;
    private final AppSwitchCollector appSwitchCollector;
    private final ScreenEventCollector screenEventCollector;

    private boolean started;

    public TelemetryProcessorImpl(
            UserPreferencesDao preferencesDao,
            AppSwitchCollector appSwitchCollector,
            ScreenEventCollector screenEventCollector
    ) {
        this.preferencesDao = preferencesDao;
        this.appSwitchCollector = appSwitchCollector;
        this.screenEventCollector = screenEventCollector;
    }

    @Override
    public void startIfConsented() {
        if (started) {
            return;
        }
        UserPreferencesEntity preferences = preferencesDao.get();
        if (preferences == null || !preferences.isTelemetryConsented()) {
            return;
        }
        screenEventCollector.start();
        appSwitchCollector.start();
        started = true;
    }

    @Override
    public void stop() {
        if (!started) {
            return;
        }
        appSwitchCollector.stop();
        screenEventCollector.stop();
        started = false;
    }
}
