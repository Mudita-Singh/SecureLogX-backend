package com.securelogx.securelogx_backend;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Represents a detected security incident.
 *
 * Core detection fields are immutable.
 * Lifecycle state is controlled and auditable.
 * Timeline is append-only (chain-of-custody ready).
 */
public class Incident {

    // ================= CORE INCIDENT IDENTITY =================

    private final String incidentId;

    // ================= CORE INCIDENT DATA =================

    private final String ipAddress;
    private final int failedAttempts;
    private final String severity;
    private final int riskScore;

    // 🔐 FORENSIC ARTIFACT (NEW)
    private final String artifactPath;

    // ================= INCIDENT LIFECYCLE =================

    public enum IncidentStatus {
        OPEN,
        INVESTIGATING,
        MITIGATED,
        CLOSED
    }

    private IncidentStatus status;

    // ================= TIMELINE =================

    private final List<TimelineEvent> timeline = new ArrayList<>();

    // ================= CONSTRUCTOR =================

    public Incident(
            String incidentId,
            String ipAddress,
            int failedAttempts,
            String severity,
            String artifactPath
    ) {
        if (incidentId == null || incidentId.isBlank()) {
            throw new IllegalArgumentException("Incident ID is required");
        }

        this.incidentId = incidentId;
        this.ipAddress = ipAddress;
        this.failedAttempts = failedAttempts;
        this.severity = severity;
        this.artifactPath = artifactPath;
        this.riskScore = calculateRiskScore(failedAttempts, severity);

        this.status = IncidentStatus.OPEN;

        addEvent(
                "INCIDENT_CREATED",
                null,
                "Incident detected by analysis engine"
        );

        if (artifactPath != null) {
            addEvent(
                    "ARTIFACT_CREATED",
                    null,
                    "Encrypted forensic artifact generated"
            );
        }

        addEvent(
                "STATUS_SET",
                null,
                "Initial status set to OPEN"
        );
    }

    // ================= GETTERS =================

    public String getIncidentId() {
        return incidentId;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public int getFailedAttempts() {
        return failedAttempts;
    }

    public String getSeverity() {
        return severity;
    }

    public int getRiskScore() {
        return riskScore;
    }

    public IncidentStatus getStatus() {
        return status;
    }

    public String getArtifactPath() {
        return artifactPath;
    }

    public List<TimelineEvent> getTimeline() {
        return timeline;
    }

    /**
     * Returns allowed next lifecycle states for UI guidance.
     * Backend still enforces transitions strictly.
     */
    public Set<IncidentStatus> getAllowedNextStatuses() {
        switch (status) {
            case OPEN:
                return EnumSet.of(IncidentStatus.INVESTIGATING);
            case INVESTIGATING:
                return EnumSet.of(IncidentStatus.MITIGATED);
            case MITIGATED:
                return EnumSet.of(IncidentStatus.CLOSED);
            case CLOSED:
            default:
                return EnumSet.noneOf(IncidentStatus.class);
        }
    }

    // ================= RISK LOGIC =================

    private int calculateRiskScore(int attempts, String severity) {
        int score = attempts * 10;

        switch (severity.toUpperCase()) {
            case "HIGH":
                score += 40;
                break;
            case "MEDIUM":
                score += 25;
                break;
            case "LOW":
                score += 10;
                break;
            default:
                score += 0;
        }

        return Math.min(score, 100);
    }

    // ================= LIFECYCLE CONTROL =================

    public void updateStatus(IncidentStatus newStatus, String analyst) {
        if (newStatus == null) {
            throw new IllegalArgumentException("Incident status cannot be null");
        }

        if (!isValidTransition(this.status, newStatus)) {
            throw new IllegalStateException(
                    "Invalid incident status transition: " +
                            this.status + " → " + newStatus
            );
        }

        IncidentStatus oldStatus = this.status;
        this.status = newStatus;

        addEvent(
                "STATUS_CHANGED",
                analyst,
                "Status changed from " + oldStatus + " to " + newStatus
        );
    }

    private boolean isValidTransition(IncidentStatus from, IncidentStatus to) {
        switch (from) {
            case OPEN:
                return to == IncidentStatus.INVESTIGATING;
            case INVESTIGATING:
                return to == IncidentStatus.MITIGATED;
            case MITIGATED:
                return to == IncidentStatus.CLOSED;
            case CLOSED:
            default:
                return false;
        }
    }

    // ================= FORENSIC / AUDIT EVENTS =================

    public void recordEvidenceAccess(String actor, String reason) {
        if (actor == null || actor.isBlank()) {
            throw new IllegalArgumentException("Actor is required for evidence access logging");
        }
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Access reason is required for forensic audit");
        }

        addEvent(
                "EVIDENCE_ACCESSED",
                actor,
                reason
        );
    }

    // ================= TIMELINE LOGIC =================

    private void addEvent(String action, String actor, String note) {
        timeline.add(new TimelineEvent(
                action,
                actor,
                note,
                LocalDateTime.now()
        ));
    }

    public void addAnalystNote(String analyst, String note) {
        if (note == null || note.isBlank()) {
            throw new IllegalArgumentException("Analyst note cannot be empty");
        }

        addEvent(
                "ANALYST_NOTE",
                analyst,
                note
        );
    }

    // ================= TIMELINE EVENT =================

    public static class TimelineEvent {

        private final String action;
        private final String actor;
        private final String note;
        private final LocalDateTime timestamp;

        public TimelineEvent(String action, String actor, String note, LocalDateTime timestamp) {
            this.action = action;
            this.actor = actor;
            this.note = note;
            this.timestamp = timestamp;
        }

        public String getAction() {
            return action;
        }

        public String getActor() {
            return actor;
        }

        public String getNote() {
            return note;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }
    }
}
