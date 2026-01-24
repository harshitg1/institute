package com.institute.Institue.controller;

import com.institute.Institue.dto.AuthRequest;
import com.institute.Institue.dto.AuthResponse;
import com.institute.Institue.dto.RegisterRequest;
import com.institute.Institue.dto.RegisterResponse;
import com.institute.Institue.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth") // Standard practice to group auth endpoints
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Public Registration: Creates a new Organization and an Admin user
     */
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@RequestBody RegisterRequest request) {
        RegisterResponse resp = authService.register(request);

        // If registration logic fails, Service usually throws an exception,
        // but we add a check here just in case.
        if (resp.getAccessToken() == null) {
            return ResponseEntity.status(400).build();
        }
        return ResponseEntity.ok(resp);
    }

    /**
     * Login: Authenticates user and returns Access + Refresh tokens
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        AuthResponse resp = authService.authenticate(request);

        // Validate if tokens were generated
        if (resp.getAccessToken() == null || resp.getAccessToken().isBlank()) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(resp);
    }

    /**
     * Refresh: Accepts a Refresh Token and returns a new Access Token
     * The refresh token is usually sent in the body or as a Header
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        try {
            AuthResponse resp = authService.refreshToken(refreshToken);
            return ResponseEntity.ok(resp);
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).build();
        }
    }
}