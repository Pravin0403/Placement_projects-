package com.focusflow.data.remote.dto;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PredictionBatchRequest {
    @SerializedName("predictions")
    public List<PredictionDto> predictions;

    public PredictionBatchRequest(List<PredictionDto> predictions) {
        this.predictions = predictions;
    }
}
