package com.focusflow.worker;

import android.content.Context;

import androidx.work.BackoffPolicy;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

public final class WorkScheduler {

    private WorkScheduler() {
    }

    public static void schedule(Context context) {
        WorkManager manager = WorkManager.getInstance(context);
        PeriodicWorkRequest prediction = new PeriodicWorkRequest.Builder(PredictionWorker.class, 15, TimeUnit.MINUTES)
                .build();
        manager.enqueueUniquePeriodicWork("prediction", ExistingPeriodicWorkPolicy.KEEP, prediction);

        PeriodicWorkRequest sync = new PeriodicWorkRequest.Builder(SyncWorker.class, 15, TimeUnit.MINUTES)
                .setConstraints(new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 1, TimeUnit.MINUTES)
                .build();
        manager.enqueueUniquePeriodicWork("sync", ExistingPeriodicWorkPolicy.KEEP, sync);

        PeriodicWorkRequest model = new PeriodicWorkRequest.Builder(ModelUpdateWorker.class, 1, TimeUnit.DAYS)
                .setConstraints(new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .build();
        manager.enqueueUniquePeriodicWork("model-update", ExistingPeriodicWorkPolicy.KEEP, model);
    }
}
