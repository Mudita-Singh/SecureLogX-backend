package com.securelogx.securelogx_backend.dto;

public class LoginRequest {

    private String username;
    private String password;

    public LoginRequest() {
        // Required by Jackson
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}

