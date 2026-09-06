package com.focusflow.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class ActiveModelResponse {
    @SerializedName("version")
    public String version;
    @SerializedName("artifactUrl")
    public String artifactUrl;
    @SerializedName("f1Score")
    public float f1Score;
}
