package com.securelogx.securelogx_backend.controller;

import com.securelogx.securelogx_backend.SecureLogXService;
import com.securelogx.securelogx_backend.dto.AnalyzeRequest;
import com.securelogx.securelogx_backend.dto.ApiResponse;
import com.securelogx.securelogx_backend.dto.DecryptRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/securelogx")
public class SecureLogXController {

    private final SecureLogXService service;

    public SecureLogXController(SecureLogXService service) {
        this.service = service;
    }

    @PostMapping("/analyze")
    public ResponseEntity<ApiResponse> analyze(@RequestBody AnalyzeRequest request) {
        try {
            String encPath = service.analyzeAndEncrypt(
                    request.getLogPath(),
                    request.getPassword()
            );

            return ResponseEntity.ok(
                    new ApiResponse(true, "Analysis & encryption successful", encPath)
            );

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    @PostMapping("/decrypt")
    public ResponseEntity<ApiResponse> decrypt(@RequestBody DecryptRequest request) {
        try {
            String outPath = service.decryptReport(
                    request.getEncryptedPath(),
                    request.getPassword()
            );

            return ResponseEntity.ok(
                    new ApiResponse(true, "Decryption successful", outPath)
            );

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, e.getMessage(), null));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, e.getMessage(), null));
        }
    }
}
