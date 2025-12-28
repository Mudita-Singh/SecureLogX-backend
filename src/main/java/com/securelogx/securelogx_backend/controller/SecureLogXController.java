package com.securelogx.securelogx_backend.controller;

import com.securelogx.securelogx_backend.Incident;
import com.securelogx.securelogx_backend.SecureLogXService;
import com.securelogx.securelogx_backend.dto.AnalyzeRequest;
import com.securelogx.securelogx_backend.dto.ApiResponse;
import com.securelogx.securelogx_backend.dto.DecryptRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@CrossOrigin(
        origins = "http://localhost:8080",
        allowCredentials = "true"
)
@RestController
@RequestMapping("/securelogx")
public class SecureLogXController {

    private final SecureLogXService service;

    public SecureLogXController(SecureLogXService service) {
        this.service = service;
    }

    // ================= AUTH HELPERS =================

    private boolean isAuthenticated(HttpSession session) {
        return session != null && session.getAttribute("AUTHENTICATED_USER") != null;
    }

    private String getAuthenticatedUser(HttpSession session) {
        return (String) session.getAttribute("AUTHENTICATED_USER");
    }

    // ================= ANALYZE (PATH BASED) =================

    @PostMapping("/analyze")
    public ResponseEntity<ApiResponse> analyze(
            @RequestBody AnalyzeRequest request,
            HttpSession session
    ) {
        if (!isAuthenticated(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(false, "Authentication required.", null));
        }

        try {
            Object incidents = service.analyzeAndReturnIncidents(
                    request.getLogPath(),
                    getAuthenticatedUser(session)
            );

            return ResponseEntity.ok(
                    new ApiResponse(true, "Analysis completed. Incidents detected.", incidents)
            );

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    // ================= ANALYZE (FILE UPLOAD) =================

    @PostMapping("/analyze/upload")
    public ResponseEntity<ApiResponse> analyzeUploadedFile(
            @RequestParam("file") MultipartFile file,
            HttpSession session
    ) {
        if (!isAuthenticated(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(false, "Authentication required.", null));
        }

        if (file == null || file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "Uploaded file is empty.", null));
        }

        Path tempFile = null;

        try {
            tempFile = Files.createTempFile("securelogx-upload-", ".log");
            file.transferTo(tempFile.toFile());

            Object incidents = service.analyzeAndReturnIncidents(
                    tempFile.toString(),
                    getAuthenticatedUser(session)
            );

            return ResponseEntity.ok(
                    new ApiResponse(true, "Uploaded log analyzed successfully.", incidents)
            );

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, e.getMessage(), null));
        } finally {
            try {
                if (tempFile != null) Files.deleteIfExists(tempFile);
            } catch (Exception ignored) {}
        }
    }

    // ================= INCIDENT READ =================

    @GetMapping("/incidents")
    public ResponseEntity<ApiResponse> getAllIncidents(HttpSession session) {
        if (!isAuthenticated(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(false, "Authentication required.", null));
        }

        return ResponseEntity.ok(
                new ApiResponse(true, "Incidents retrieved.", service.getAllIncidents())
        );
    }

    @GetMapping("/incidents/{incidentId}")
    public ResponseEntity<ApiResponse> getIncident(
            @PathVariable String incidentId,
            HttpSession session
    ) {
        if (!isAuthenticated(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(false, "Authentication required.", null));
        }

        try {
            Incident incident = service.getIncidentById(incidentId);

            Map<String, Object> payload = new HashMap<>();
            payload.put("incident", incident);
            payload.put("allowedNextStatuses", incident.getAllowedNextStatuses());

            return ResponseEntity.ok(
                    new ApiResponse(true, "Incident retrieved.", payload)
            );

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, "Incident not found.", null));
        }
    }

    // ================= INCIDENT STATUS UPDATE =================

    @PostMapping("/incidents/{incidentId}/status")
    public ResponseEntity<ApiResponse> updateIncidentStatus(
            @PathVariable String incidentId,
            @RequestBody StatusUpdateRequest request,
            HttpSession session
    ) {
        if (!isAuthenticated(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(false, "Authentication required.", null));
        }

        try {
            Incident incident = service.getIncidentById(incidentId);

            Incident.IncidentStatus newStatus =
                    Incident.IncidentStatus.valueOf(request.getStatus().toUpperCase());

            incident.updateStatus(
                    newStatus,
                    getAuthenticatedUser(session)
            );

            Map<String, Object> payload = new HashMap<>();
            payload.put("incident", incident);
            payload.put("allowedNextStatuses", incident.getAllowedNextStatuses());

            return ResponseEntity.ok(
                    new ApiResponse(true, "Incident status updated successfully.", payload)
            );

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, e.getMessage(), null));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse(false, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Failed to update incident status.", null));
        }
    }

    // ================= DECRYPT (FORENSICS ONLY) =================

    @PostMapping("/decrypt")
    public ResponseEntity<ApiResponse> decrypt(
            @RequestBody DecryptRequest request,
            HttpSession session
    ) {
        if (!isAuthenticated(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(false, "Authentication required.", null));
        }

        try {
            Object report = service.decryptAndReadReport(
                    request.getEncryptedPath(),
                    request.getPassword(),
                    getAuthenticatedUser(session)
            );

            return ResponseEntity.ok(
                    new ApiResponse(true, "Decryption successful", report)
            );

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse(false, e.getMessage(), null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Failed to decrypt incident report.", null));
        }
    }

    // ================= REQUEST DTO (LOCAL) =================

    public static class StatusUpdateRequest {
        private String status;

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}
