package com.focusflow.worker;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.focusflow.FocusFlowApplication;
import com.focusflow.data.remote.dto.ActiveModelResponse;

import retrofit2.Response;

public class ModelUpdateWorker extends Worker {

    public ModelUpdateWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            Response<ActiveModelResponse> response = ((FocusFlowApplication) getApplicationContext())
                    .getAppContainer()
                    .api()
                    .activeModel()
                    .execute();
            if (!response.isSuccessful()) {
                return Result.retry();
            }
            ActiveModelResponse model = response.body();
            if (model == null || model.artifactUrl == null || model.artifactUrl.trim().isEmpty()) {
                return Result.success();
            }
            String version = model.version == null ? "" : model.version;
            if (!version.equals(((FocusFlowApplication) getApplicationContext())
                    .getAppContainer().modelManager().loadedVersion())) {
                ((FocusFlowApplication) getApplicationContext())
                        .getAppContainer()
                        .modelManager()
                        .downloadAndActivate(model.artifactUrl, version);
            }
            return Result.success();
        } catch (Exception exception) {
            return Result.retry();
        }
    }
}
