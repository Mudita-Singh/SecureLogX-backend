package com.securelogx.securelogx_backend;

public class Config {

    public static int getFailedAttemptThreshold() {
        return 3;
    }

    public static int getTimeWindowMinutes() {
        return 5;
    }
}
