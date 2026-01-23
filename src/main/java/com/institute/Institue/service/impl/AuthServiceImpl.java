package com.institute.Institue.service.impl;

import com.institute.Institue.dto.AuthRequest;
import com.institute.Institue.dto.AuthResponse;
import com.institute.Institue.service.AuthService;
import com.institute.Institue.repository.UserRepository;
import com.institute.Institue.security.JwtService;
import com.institute.Institue.model.User;
import com.institute.Institue.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.List;
import java.util.Comparator;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final UserMapper userMapper;

    public AuthServiceImpl(UserRepository userRepository, JwtService jwtService, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.userMapper = userMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse authenticate(AuthRequest request) {
        if (request == null || request.getEmail() == null) {
            log.debug("Authentication failed: missing credentials");
            return new AuthResponse("", null, null);
        }

        Optional<User> maybe = userRepository.findByEmailWithRoles(request.getEmail());
        if (maybe.isEmpty()) {
            log.debug("Authentication failed: no such user {}", request.getEmail());
            return new AuthResponse("", null, null);
        }

        User user = maybe.get();
        List<String> roles = userMapper.mapRoles(user.getRoles());

        // Determine primary role as a single string
        String primaryRole = null;
        if (roles != null && !roles.isEmpty()) {
            primaryRole = roles.stream()
                    .sorted(Comparator.comparingInt(AuthServiceImpl::rolePriority))
                    .findFirst()
                    .orElse(null);
        }

        String token = jwtService.generateToken(
                user.getEmail(),
                user.getOrganizationId() == null ? null : user.getOrganizationId().toString(),
                roles
        );
        log.info("Generated JWT for user {} with roles={} org={}", user.getEmail(), roles, user.getOrganizationId());
        return new AuthResponse(
                token,
                user.getOrganizationId() == null ? null : user.getOrganizationId().toString(),
                primaryRole
        );
    }

    private static int rolePriority(String r) {
        if (r == null) return Integer.MAX_VALUE;
        return switch (r.toUpperCase()) {
            case "SUPER_ADMIN" -> 0;
            case "ORG_ADMIN" -> 1;
            case "TUTOR" -> 2;
            case "STUDENT" -> 3;
            default -> 9;
        };
    }
}
