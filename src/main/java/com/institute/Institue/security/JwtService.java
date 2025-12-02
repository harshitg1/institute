package com.institute.Institue.security;

import org.springframework.stereotype.Component;

@Component
public class JwtService {

    public String generateToken(String subject) {
        // Skeleton implementation
        return "jwt-token-for-" + subject;
    }

    public boolean validateToken(String token) {
        return token != null && token.startsWith("jwt-token-for-");
    }
}

