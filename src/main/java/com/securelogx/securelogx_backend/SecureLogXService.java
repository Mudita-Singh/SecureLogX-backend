package com.securelogx.securelogx_backend;

import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SecureLogXService {

    private final Map<String, Incident> incidentStore = new ConcurrentHashMap<>();
    private LocalDateTime lastAnalysisTime;

    private final LogReader logReader = new LogReader();
    private final LogAnalyzer logAnalyzer = new LogAnalyzer();

    private static final String REPORT_DIR = "reports";

    /**
     * Analyze logs, generate encrypted artifact, store incidents
     */
    public Collection<Incident> analyzeAndReturnIncidents(
            String logPath,
            String password
    ) {
        try {
            Files.createDirectories(Path.of(REPORT_DIR));

            // 1️⃣ Read logs
            List<String> logs = logReader.readLogs(logPath);

            // 2️⃣ Analyze
            List<Incident> rawIncidents = logAnalyzer.analyze(logs);

            // 3️⃣ Generate report content (simple & explainable)
            StringBuilder report = new StringBuilder();
            report.append("SecureLogX Incident Report\n");
            report.append("Generated at: ").append(LocalDateTime.now()).append("\n\n");

            for (String line : logs) {
                report.append(line).append("\n");
            }

            // 4️⃣ Encrypt report
            String artifactName = "incident-report-" + UUID.randomUUID() + ".enc";
            Path artifactPath = Path.of(REPORT_DIR, artifactName);
            encrypt(report.toString(), password, artifactPath);

            // 5️⃣ Store enriched incidents
            List<Incident> incidents = new ArrayList<>();
            for (Incident i : rawIncidents) {
                Incident enriched = new Incident(
                        i.getIncidentId(),
                        i.getIpAddress(),
                        i.getFailedAttempts(),
                        i.getSeverity(),
                        artifactPath.toString()
                );
                incidentStore.put(enriched.getIncidentId(), enriched);
                incidents.add(enriched);
            }

            lastAnalysisTime = LocalDateTime.now();
            return incidents;

        } catch (Exception e) {
            throw new RuntimeException("Analysis failed: " + e.getMessage());
        }
    }

    /**
     * SOC dashboard
     */
    public Collection<Incident> getAllIncidents() {
        return incidentStore.values();
    }

    /**
     * Get single incident
     */
    public Incident getIncidentById(String incidentId) {
        Incident incident = incidentStore.get(incidentId);
        if (incident == null) {
            throw new IllegalArgumentException("Incident not found: " + incidentId);
        }
        return incident;
    }

    /**
     * Last analysis timestamp
     */
    public LocalDateTime getLastAnalysisTime() {
        return lastAnalysisTime;
    }

    /**
     * Decrypt forensic artifact
     */
    public String decryptAndReadReport(
            String encryptedPath,
            String password,
            String username
    ) {
        try {
            byte[] encrypted = Files.readAllBytes(Path.of(encryptedPath));

            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(
                    Cipher.DECRYPT_MODE,
                    new SecretKeySpec(Arrays.copyOf(password.getBytes(), 16), "AES")
            );

            String decrypted = new String(cipher.doFinal(encrypted));

            // Log forensic access
            incidentStore.values().stream()
                    .filter(i -> encryptedPath.equals(i.getArtifactPath()))
                    .forEach(i ->
                            i.recordEvidenceAccess(
                                    username,
                                    "Decrypted forensic artifact"
                            )
                    );

            return decrypted;

        } catch (Exception e) {
            throw new SecurityException("Invalid password or artifact path");
        }
    }

    // ================= ENCRYPTION UTILITY =================

    private void encrypt(String content, String password, Path output) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(
                Cipher.ENCRYPT_MODE,
                new SecretKeySpec(Arrays.copyOf(password.getBytes(), 16), "AES")
        );
        Files.write(output, cipher.doFinal(content.getBytes()));
    }
}
