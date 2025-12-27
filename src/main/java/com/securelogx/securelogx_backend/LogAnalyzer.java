package com.securelogx.securelogx_backend;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LogAnalyzer {

    public List<Incident> analyze(List<String> logs) {

        Map<String, Integer> failedAttempts = new HashMap<>();

        for (String line : logs) {
            if (line.contains("Failed password")) {
                String ip = extractIp(line);
                failedAttempts.put(ip, failedAttempts.getOrDefault(ip, 0) + 1);
            }
        }

        List<Incident> incidents = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : failedAttempts.entrySet()) {
            String severity = entry.getValue() >= 3 ? "HIGH" : "LOW";
            incidents.add(new Incident(entry.getKey(), entry.getValue(), severity));
        }

        return incidents;
    }

    private String extractIp(String logLine) {
        int start = logLine.lastIndexOf("from ");
        return start != -1 ? logLine.substring(start + 5).trim() : "UNKNOWN";
    }
}
