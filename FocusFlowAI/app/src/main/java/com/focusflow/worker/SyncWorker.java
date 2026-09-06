package com.focusflow.worker;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.focusflow.FocusFlowApplication;
import com.focusflow.core.di.AppContainer;
import com.focusflow.data.local.AppSettings;

public class SyncWorker extends Worker {

    public SyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        AppContainer container = ((FocusFlowApplication) getApplicationContext()).getAppContainer();
        AppSettings settings = container.appSettings();
        try {
            boolean synced = container.syncClient().sync();
            if (!synced && settings.isCircuitOpen()) {
                return Result.retry();
            }
            return Result.success();
        } catch (Exception exception) {
            settings.recordSyncFailure();
            return Result.retry();
        }
    }
}
