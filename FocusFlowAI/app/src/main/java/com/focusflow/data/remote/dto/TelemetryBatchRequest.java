package com.focusflow.data.remote.dto;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class TelemetryBatchRequest {
    @SerializedName("events")
    public List<TelemetryEventDto> events;

    public TelemetryBatchRequest(List<TelemetryEventDto> events) {
        this.events = events;
    }
}
