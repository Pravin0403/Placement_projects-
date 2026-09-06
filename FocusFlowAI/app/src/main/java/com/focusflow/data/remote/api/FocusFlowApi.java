package com.focusflow.data.remote.api;

import com.focusflow.data.remote.dto.ActiveModelResponse;
import com.focusflow.data.remote.dto.AuthRequest;
import com.focusflow.data.remote.dto.AuthResponse;
import com.focusflow.data.remote.dto.FeedbackDto;
import com.focusflow.data.remote.dto.PredictionBatchRequest;
import com.focusflow.data.remote.dto.RefreshRequest;
import com.focusflow.data.remote.dto.TelemetryBatchRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface FocusFlowApi {

    @POST("auth/register")
    Call<AuthResponse> register(@Body AuthRequest request);

    @POST("auth/login")
    Call<AuthResponse> login(@Body AuthRequest request);

    @POST("auth/refresh")
    Call<AuthResponse> refresh(@Body RefreshRequest request);

    @POST("telemetry/batch")
    Call<Void> uploadTelemetry(
            @Header("Idempotency-Key") String idempotencyKey,
            @Body TelemetryBatchRequest request
    );

    @POST("predictions/batch")
    Call<Void> uploadPredictions(
            @Header("Idempotency-Key") String idempotencyKey,
            @Body PredictionBatchRequest request
    );

    @POST("feedback")
    Call<Void> uploadFeedback(@Body FeedbackDto request);

    @GET("models/active")
    Call<ActiveModelResponse> activeModel();
}
