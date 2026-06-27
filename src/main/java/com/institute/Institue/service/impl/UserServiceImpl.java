package com.institute.Institue.service.impl;

import com.institute.Institue.dto.CreateUserRequest;
import com.institute.Institue.dto.UserResponse;
import com.institute.Institue.exception.BadRequestException;
import com.institute.Institue.exception.DuplicateResourceException;
import com.institute.Institue.exception.ResourceNotFoundException;
import com.institute.Institue.model.Organization;
import com.institute.Institue.model.Role;
import com.institute.Institue.model.User;
import com.institute.Institue.model.enums.UserRole;
import com.institute.Institue.mapper.UserMapper;
import com.institute.Institue.repository.OrganizationRepository;
import com.institute.Institue.repository.RoleRepository;
import com.institute.Institue.repository.UserRepository;
import com.institute.Institue.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final String DEFAULT_CREATED_USER_PASSWORD = "ChangeMe123!";

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Override
    public UserResponse createUser(String orgId, CreateUserRequest req) {
        UUID organizationId = UUID.fromString(orgId);
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", orgId));

        if (req.getEmail() == null || req.getEmail().isBlank()) {
            throw new BadRequestException("Email is required", "EMAIL_REQUIRED");
        }
        if (req.getRoles() == null || req.getRoles().isBlank()) {
            throw new BadRequestException("Role is required", "ROLE_REQUIRED");
        }
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new DuplicateResourceException("User with email '" + req.getEmail() + "' already exists");
        }

        UserRole requestedRole = parseRequestedRole(req.getRoles());
        if (requestedRole == UserRole.SUPER_ADMIN) {
            throw new BadRequestException("SUPER_ADMIN cannot be created within an organization", "INVALID_ROLE");
        }

        Role role = roleRepository.findByRole(requestedRole)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "name", requestedRole.name()));

        User user = User.builder()
                .email(req.getEmail())
                .password(passwordEncoder.encode(
                        req.getPassword() == null || req.getPassword().isBlank()
                                ? DEFAULT_CREATED_USER_PASSWORD
                                : req.getPassword()))
                .firstName(req.getFirstName())
                .lastName(req.getLastName())
                .organization(organization)
                .role(role)
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .build();

        User saved = userRepository.save(user);
        log.info("Created organization user '{}' with role {}", saved.getEmail(), requestedRole);
        return userMapper.toDto(saved);
    }

    @Override
    public List<UserResponse> listUsers(String orgId) {
        UUID organizationId = UUID.fromString(orgId);
        return userRepository.findByOrganization_Id(organizationId).stream()
                .map(userMapper::toDto)
                .toList();
    }

    private UserRole parseRequestedRole(String rawRoles) {
        String normalized = rawRoles.split(",")[0].trim().toUpperCase();
        if ("INSTRUCTOR".equals(normalized)) {
            normalized = "TUTOR";
        }

        try {
            return UserRole.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Unsupported role: " + normalized, "INVALID_ROLE");
        }
    }

}
