package com.securelogx.securelogx_backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Generates versioned, integrity-protected incident evidence.
 *
 * Evidence is NEVER overwritten.
 * Each regeneration produces a new sealed artifact.
 */
public class ReportGenerator {

    private final String reportsDir;

    public ReportGenerator(String reportsDir) {
        this.reportsDir = reportsDir;
    }

    // ================= INITIAL EVIDENCE GENERATION =================

    /**
     * Generates initial immutable evidence (v1).
     */
    public String generate(List<Incident> incidents, String createdBy) throws Exception {
        return generateVersionedEvidence(
                incidents,
                createdBy,
                1,
                "Initial incident analysis"
        );
    }

    // ================= EVIDENCE RE-GENERATION =================

    /**
     * Generates a new version of evidence after lifecycle or audit updates.
     * Old evidence is preserved.
     */
    public String regenerate(
            List<Incident> incidents,
            String createdBy,
            int newVersion,
            String reason
    ) throws Exception {

        if (newVersion <= 1) {
            throw new IllegalArgumentException("Evidence version must be greater than 1");
        }

        return generateVersionedEvidence(
                incidents,
                createdBy,
                newVersion,
                reason
        );
    }

    // ================= CORE GENERATION LOGIC =================

    private String generateVersionedEvidence(
            List<Incident> incidents,
            String createdBy,
            int version,
            String reason
    ) throws Exception {

        File dir = new File(reportsDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));

        String fileName =
                "incident_report_" + timestamp + "_v" + version + ".json";

        File reportFile = new File(dir, fileName);

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // 1️⃣ Serialize incident payload FIRST (authoritative content)
        String incidentJson = mapper.writeValueAsString(incidents);

        // 2️⃣ Compute integrity hash
        String evidenceHash = computeSha256(incidentJson);

        // 3️⃣ Wrap evidence
        EvidencePayload payload = new EvidencePayload(
                new EvidenceMetadata(
                        createdBy,
                        LocalDateTime.now(),
                        version,
                        reason,
                        evidenceHash
                ),
                incidents
        );

        mapper.writerWithDefaultPrettyPrinter()
                .writeValue(reportFile, payload);

        return reportFile.getPath();
    }

    // ================= HASH LOGIC =================

    private String computeSha256(String input) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));

        StringBuilder hex = new StringBuilder();
        for (byte b : hash) {
            hex.append(String.format("%02x", b));
        }
        return hex.toString();
    }

    // ================= EVIDENCE STRUCTURE =================

    private static class EvidencePayload {
        public final EvidenceMetadata metadata;
        public final List<Incident> incidents;

        public EvidencePayload(EvidenceMetadata metadata, List<Incident> incidents) {
            this.metadata = metadata;
            this.incidents = incidents;
        }
    }

    private static class EvidenceMetadata {
        public final String createdBy;
        public final LocalDateTime createdAt;
        public final int version;
        public final String reason;
        public final String evidenceHash;

        public EvidenceMetadata(
                String createdBy,
                LocalDateTime createdAt,
                int version,
                String reason,
                String evidenceHash
        ) {
            this.createdBy = createdBy;
            this.createdAt = createdAt;
            this.version = version;
            this.reason = reason;
            this.evidenceHash = evidenceHash;
        }
    }
}
