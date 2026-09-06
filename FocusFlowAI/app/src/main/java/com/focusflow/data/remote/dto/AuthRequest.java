package com.focusflow.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class AuthRequest {
    @SerializedName("email")
    public String email;
    @SerializedName("password")
    public String password;

    public AuthRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }
}
