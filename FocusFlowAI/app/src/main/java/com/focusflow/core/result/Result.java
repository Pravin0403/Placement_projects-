package com.focusflow.core.result;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public final class Result<T> {

    public enum Status {
        SUCCESS,
        ERROR,
        LOADING
    }

    private final Status status;
    @Nullable
    private final T data;
    @Nullable
    private final String message;

    private Result(Status status, @Nullable T data, @Nullable String message) {
        this.status = status;
        this.data = data;
        this.message = message;
    }

    public static <T> Result<T> success(@NonNull T data) {
        return new Result<>(Status.SUCCESS, data, null);
    }

    public static <T> Result<T> error(@NonNull String message) {
        return new Result<>(Status.ERROR, null, message);
    }

    public static <T> Result<T> loading() {
        return new Result<>(Status.LOADING, null, null);
    }

    public Status status() {
        return status;
    }

    @Nullable
    public T data() {
        return data;
    }

    @Nullable
    public String message() {
        return message;
    }

    public boolean isSuccess() {
        return status == Status.SUCCESS;
    }
}
