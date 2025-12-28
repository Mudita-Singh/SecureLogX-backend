package com.securelogx.securelogx_backend.controller;

import com.securelogx.securelogx_backend.auth.UserStore;
import com.securelogx.securelogx_backend.dto.ApiResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@CrossOrigin(
        origins = "http://localhost:8080",
        allowCredentials = "true"
)
public class LoginController {

    // ===== LOGIN =====
    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(
            @RequestBody LoginRequest req,
            HttpSession session
    ) {
        if (req == null || req.username == null || req.password == null) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Username and password required.", null));
        }

        if (!UserStore.userExists(req.username)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, "Account does not exist.", null));
        }

        if (!UserStore.validate(req.username, req.password)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(false, "Invalid credentials.", null));
        }

        session.setAttribute("AUTHENTICATED_USER", req.username);
        return ResponseEntity.ok(
                new ApiResponse(true, "Login successful.", null)
        );
    }

    // ===== SIGNUP =====
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse> signup(
            @RequestBody LoginRequest req,
            HttpSession session
    ) {
        if (req == null || req.username == null || req.password == null) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Username and password required.", null));
        }

        if (UserStore.userExists(req.username)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse(false, "Account already exists.", null));
        }

        try {
            UserStore.createUser(req.username, req.password);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse(false, "Failed to create account.", null));
        }

        session.setAttribute("AUTHENTICATED_USER", req.username);

        return ResponseEntity.ok(
                new ApiResponse(true, "Account created and logged in.", null)
        );
    }

    // ===== AUTH CHECK =====
    @GetMapping("/me")
    public ResponseEntity<ApiResponse> me(HttpSession session) {
        Object user = session.getAttribute("AUTHENTICATED_USER");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(false, "Not authenticated", null));
        }
        return ResponseEntity.ok(
                new ApiResponse(true, "Authenticated", user)
        );
    }

    // ===== LOGOUT =====
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok(
                new ApiResponse(true, "Logged out", null)
        );
    }

    // ===== DTO =====
    static class LoginRequest {
        public String username;
        public String password;
    }
}
