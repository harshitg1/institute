package com.institute.Institue.service.impl;

import com.institute.Institue.dto.AuthRequest;
import com.institute.Institue.dto.AuthResponse;
import com.institute.Institue.service.AuthService;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    @Override
    public AuthResponse authenticate(AuthRequest request) {
        return new AuthResponse("token-for-" + (request != null ? request.getUsername() : "unknown"));
    }
}

