package com.focusflow.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class PredictionDto {
    @SerializedName("predictionId")
    public String predictionId;
    @SerializedName("sessionId")
    public String sessionId;
    @SerializedName("riskScore")
    public float riskScore;
    @SerializedName("modelVersion")
    public String modelVersion;
    @SerializedName("createdAt")
    public long createdAt;
}
