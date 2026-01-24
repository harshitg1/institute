package com.institute.Institue.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String accessToken;  // Formerly 'token'
    private String refreshToken; // New field
    private String organizationId;
    private String role;         // Role name (e.g., "TUTOR")

    // If you need to keep a single-argument constructor for legacy reasons:
    public AuthResponse(String accessToken) {
        this.accessToken = accessToken;
    }
}