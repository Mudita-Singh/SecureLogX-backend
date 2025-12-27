package com.securelogx.securelogx_backend.dto;

public class AnalyzeRequest {

    private String logPath;
    private String password;

    public AnalyzeRequest() {}

    public String getLogPath() {
        return logPath;
    }

    public void setLogPath(String logPath) {
        this.logPath = logPath;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
