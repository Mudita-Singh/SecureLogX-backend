package com.securelogx.securelogx_backend;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class LogAnalyzer {

    public List<Incident> analyze(List<String> logs) {

        // Track failed attempts per IP
        Map<String, Integer> failedAttempts = new HashMap<>();

        for (String line : logs) {
            if (line.contains("Failed password")) {
                String ip = extractIp(line);
                failedAttempts.put(ip, failedAttempts.getOrDefault(ip, 0) + 1);
            }
        }

        List<Incident> incidents = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : failedAttempts.entrySet()) {

            int attempts = entry.getValue();
            String severity = determineSeverity(attempts);

            String incidentId = UUID.randomUUID().toString();

            // 🔐 TEMP artifact path (realistic, SOC-style)
            String artifactPath = "reports/incident-" + incidentId + ".enc";

            incidents.add(
                    new Incident(
                            incidentId,
                            entry.getKey(),
                            attempts,
                            severity,
                            artifactPath
                    )
            );
        }

        return incidents;
    }

    private String determineSeverity(int attempts) {
        if (attempts >= 5) return "HIGH";
        if (attempts >= 3) return "MEDIUM";
        return "LOW";
    }

    private String extractIp(String logLine) {
        int start = logLine.lastIndexOf("from ");
        return start != -1
                ? logLine.substring(start + 5).trim()
                : "UNKNOWN";
    }
}

