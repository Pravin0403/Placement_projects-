package com.focusflow.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class FeedbackDto {
    @SerializedName("predictionId")
    public String predictionId;
    @SerializedName("helpful")
    public boolean helpful;
    @SerializedName("actionTaken")
    public String actionTaken;
    @SerializedName("createdAt")
    public long createdAt;
}
