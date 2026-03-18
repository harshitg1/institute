package com.institute.Institue.service.impl;

import com.institute.Institue.dto.*;
import com.institute.Institue.model.Organization;
import com.institute.Institue.model.Role;
import com.institute.Institue.model.User;
import com.institute.Institue.repository.OrganizationRepository;
import com.institute.Institue.repository.RoleRepository;
import com.institute.Institue.repository.UserRepository;
import com.institute.Institue.security.JwtService;
import com.institute.Institue.security.RoleNames;
import com.institute.Institue.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final RoleRepository roleRepository;
    private final OrganizationRepository organizationRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public AuthResponse authenticate(AuthRequest request) {
        log.debug("Attempting to authenticate user: {}", request.getEmail());

        // 1. Find User
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        // 2. Verify Password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.debug("Authentication failed: wrong password for {}", request.getEmail());
            throw new RuntimeException("Invalid email or password");
        }


        // 3. Prepare Data for Tokens
        String orgId = (user.getOrganization() != null)
                ? user.getOrganization().getId().toString()
                : null;

        String roleId = user.getRole().getId().toString();
        String roleName = user.getRole().getRole().name();

        // 4. Generate Tokens
        String accessToken = jwtService.generateAccessToken(user.getEmail(), orgId, roleId, List.of(roleName));
        String refreshToken = jwtService.generateRefreshToken(user.getEmail());

        log.info("User {} authenticated successfully", user.getEmail());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .organizationId(orgId)
                .role(roleName)
                .build();
    }

    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        log.info("Registering new organization and admin: {}", request.getEmail());

        // 1. Validation
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("User with email " + request.getEmail() + " already exists");
        }

        // 2. Create Organization
        Organization org = Organization.builder()
                .name(request.getFirstName())
                .build();
        Organization savedOrg = organizationRepository.save(org);

        // 3. Fetch Admin Role
        Role adminRole = roleRepository.findByName(RoleNames.ORG_ADMIN)
                .orElseThrow(() -> new RuntimeException("Default Role ORG_ADMIN not found"));

        // 4. Create User
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword())) // ENCRYPT
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .organization(savedOrg)
                .role(adminRole) // SET SINGLE ROLE
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .build();

        User savedUser = userRepository.save(user);

        // 5. Generate Tokens
        String accessToken = jwtService.generateAccessToken(
                savedUser.getEmail(),
                savedOrg.getId().toString(),
                adminRole.getId().toString(),
                List.of(adminRole.getRole().name())
        );
        String refreshToken = jwtService.generateRefreshToken(savedUser.getEmail());

        return RegisterResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .organizationId(savedOrg.getId().toString())
                .adminUserId(savedUser.getId().toString())
                .role(adminRole.getRole().name())
                .build();
    }

    /**
     * Optional: Implement token refresh logic
     */
    @Override
    @Transactional(readOnly = true)
    public AuthResponse refreshToken(String refreshToken) {
        if (!jwtService.validateToken(refreshToken)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token expired or invalid");
        }

        Map<String, Object> claims = jwtService.parseClaims(refreshToken);
        String email = (String) claims.get("sub");

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String newAccessToken = jwtService.generateAccessToken(
                user.getEmail(),
                user.getOrganization() != null ? user.getOrganization().getId().toString() : null,
                user.getRole().getId().toString(),
                List.of(user.getRole().getRole().name())
        );

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken) // Keep same or rotate
                .organizationId(user.getOrganization() != null ? user.getOrganization().getId().toString() : null)
                .role(user.getRole().getRole().name())
                .build();
    }
}