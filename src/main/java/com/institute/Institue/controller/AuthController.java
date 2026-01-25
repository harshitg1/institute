package com.institute.Institue.controller;

import com.institute.Institue.dto.AuthRequest;
import com.institute.Institue.dto.AuthResponse;
import com.institute.Institue.dto.RegisterRequest;
import com.institute.Institue.dto.RegisterResponse;
import com.institute.Institue.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request, HttpServletResponse response) {
        AuthResponse resp = authService.authenticate(request);

        // Validate if tokens were generated
        if (resp.getAccessToken() == null || resp.getAccessToken().isBlank()) {
            return ResponseEntity.status(401).build();
        }
        // Set refresh token as an HTTP-only cookie
        Cookie refreshTokenCookie = new Cookie("refreshToken", resp.getRefreshToken());
        refreshTokenCookie.setHttpOnly(true); // Prevent access via JavaScript
        refreshTokenCookie.setSecure(true); // Use HTTPS in production
        refreshTokenCookie.setPath("/"); // Accessible across the app
        refreshTokenCookie.setMaxAge(7 * 24 * 60 * 60); // 7 days expiry
        response.addCookie(refreshTokenCookie);

        return ResponseEntity.ok(resp);
    }

    /**
     * Refresh: Accepts a Refresh Token and returns a new Access Token
     * The refresh token is usually sent in the body or as a Header
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(HttpServletRequest request) {
        String refreshToken = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("refreshToken".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }
        if (refreshToken == null) {
            throw new RuntimeException("Refresh token not found in cookies");
        }
        // Proceed with existing refresh logic using refreshToken
        AuthResponse response = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(response);
    }

}