package com.institute.Institue.controller;

import com.institute.Institue.dto.AuthRequest;
import com.institute.Institue.dto.AuthResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @GetMapping("/ping")
    public ResponseEntity<AuthResponse> ping() {
        return ResponseEntity.ok(new AuthResponse("pong"));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        // Skeleton: return a dummy response
        return ResponseEntity.ok(new AuthResponse("token-placeholder"));
    }
}

