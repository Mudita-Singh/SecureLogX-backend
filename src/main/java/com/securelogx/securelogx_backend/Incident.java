package com.securelogx.securelogx_backend;

public class Incident {

    private String ipAddress;
    private int failedAttempts;
    private String severity;

    // Required by Jackson
    public Incident() {
    }

    public Incident(String ipAddress, int failedAttempts, String severity) {
        this.ipAddress = ipAddress;
        this.failedAttempts = failedAttempts;
        this.severity = severity;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public int getFailedAttempts() {
        return failedAttempts;
    }

    public void setFailedAttempts(int failedAttempts) {
        this.failedAttempts = failedAttempts;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }
}
