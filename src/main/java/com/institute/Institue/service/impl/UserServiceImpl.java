package com.institute.Institue.service.impl;

import com.institute.Institue.dto.CreateUserRequest;
import com.institute.Institue.dto.UserResponse;
import com.institute.Institue.mapper.UserMapper;
import com.institute.Institue.model.Role;
import com.institute.Institue.model.User;
import com.institute.Institue.repository.OrganizationRepository;
import com.institute.Institue.repository.RoleRepository;
import com.institute.Institue.repository.UserRepository;
import com.institute.Institue.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor // Automatically creates constructor for all final fields
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserResponse createUser(String orgId, CreateUserRequest req) {
        UUID orgUuid = UUID.fromString(orgId);

        // 1. Validation
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new RuntimeException("User with email " + req.getEmail() + " already exists");
        }

        // 2. Fetch Single Role
        // Logic: Since your model is now Single Role, we take the first role from the input string
        String roleName = "STUDENT"; // default
        if (req.getRoles() != null && !req.getRoles().isBlank()) {
            // Split and take the first one, e.g., "TUTOR,ADMIN" -> "TUTOR"
            roleName = req.getRoles().split(",")[0].trim().toUpperCase();
        }

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found: "));

        // 3. Build and Save User
        User user = User.builder()
                .email(req.getEmail())
                // Use req.getPassword() or a default "ChangeMe123!" then encode it
                .password("ChangeMe123!")
                .organizationId(orgUuid)
                .role(role) // SET SINGLE ROLE
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .build();

        User saved = userRepository.save(user);
        return userMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> listUsers(String orgId) {
        UUID orgUuid = (orgId == null) ? null : UUID.fromString(orgId);
        if (orgUuid == null) return java.util.Collections.emptyList();

        // Use the repository method that finds by Org ID
        return userRepository.findByOrganizationId(orgUuid)
                .stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }
}