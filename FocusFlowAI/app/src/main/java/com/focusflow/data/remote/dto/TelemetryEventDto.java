package com.focusflow.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class TelemetryEventDto {
    @SerializedName("eventId")
    public String eventId;
    @SerializedName("sessionId")
    public String sessionId;
    @SerializedName("type")
    public String type;
    @SerializedName("packageName")
    public String packageName;
    @SerializedName("timestamp")
    public long timestamp;
}
