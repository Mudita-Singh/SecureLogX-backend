package com.securelogx.securelogx_backend.dto;

public class ApiResponse {

    private boolean success;
    private String message;
    private String path;

    public ApiResponse(boolean success, String message, String path) {
        this.success = success;
        this.message = message;
        this.path = path;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public String getPath() {
        return path;
    }
}
