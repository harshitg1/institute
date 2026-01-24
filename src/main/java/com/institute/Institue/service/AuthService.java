package com.institute.Institue.service;

import com.institute.Institue.dto.AuthRequest;
import com.institute.Institue.dto.AuthResponse;
import com.institute.Institue.dto.RegisterRequest;
import com.institute.Institue.dto.RegisterResponse;

public interface AuthService {
    AuthResponse authenticate(AuthRequest request);
    RegisterResponse register(RegisterRequest request);
    AuthResponse refreshToken(String refreshToken);
}

