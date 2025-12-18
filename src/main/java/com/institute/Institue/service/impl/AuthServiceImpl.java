package com.institute.Institue.service.impl;

import com.institute.Institue.dto.AuthRequest;
import com.institute.Institue.dto.AuthResponse;
import com.institute.Institue.service.AuthService;
import com.institute.Institue.repository.UserRepository;
import com.institute.Institue.security.JwtService;
import com.institute.Institue.model.User;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    public AuthServiceImpl(UserRepository userRepository, JwtService jwtService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    @Override
    public AuthResponse authenticate(AuthRequest request) {
        if (request == null || request.getUsername() == null) {
            return new AuthResponse("", null, null);
        }

        Optional<User> maybe = userRepository.findByUsername(request.getUsername());
        if (maybe.isEmpty()) {
            return new AuthResponse("", null, null);
        }

        User user = maybe.get();
        String rolesCsv = user.getRoles();
        java.util.List<String> roles = rolesCsv == null ? java.util.Collections.emptyList()
                : Arrays.stream(rolesCsv.split(",")).map(String::trim).collect(Collectors.toList());

        String token = jwtService.generateToken(user.getUsername(), user.getOrganizationId() == null ? null : user.getOrganizationId().toString(), roles);
        return new AuthResponse(token, user.getOrganizationId() == null ? null : user.getOrganizationId().toString(), rolesCsv);
    }
}
