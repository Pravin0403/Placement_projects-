package com.focusflow.worker;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.focusflow.FocusFlowApplication;

public class PredictionWorker extends Worker {

    public PredictionWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        ((FocusFlowApplication) getApplicationContext()).getAppContainer().focusEngine().onWindowReady();
        return Result.success();
    }
}
