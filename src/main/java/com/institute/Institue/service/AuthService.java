package com.institute.Institue.service;

import com.institute.Institue.dto.AuthRequest;
import com.institute.Institue.dto.AuthResponse;

public interface AuthService {
    AuthResponse authenticate(AuthRequest request);
}

