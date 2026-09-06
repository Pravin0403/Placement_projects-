package com.focusflow;

import android.app.Application;

import com.focusflow.core.di.AppContainer;
import com.focusflow.core.util.NotificationHelper;
import com.focusflow.worker.WorkScheduler;

public class FocusFlowApplication extends Application {

    private AppContainer appContainer;

    @Override
    public void onCreate() {
        super.onCreate();
        NotificationHelper.ensureChannel(this);
        appContainer = new AppContainer(this);
        WorkScheduler.schedule(this);
    }

    public AppContainer getAppContainer() {
        return appContainer;
    }
}
