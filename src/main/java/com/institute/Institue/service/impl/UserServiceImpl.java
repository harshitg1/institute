package com.institute.Institue.service.impl;

import com.institute.Institue.dto.CreateUserRequest;
import com.institute.Institue.dto.UserResponse;
import com.institute.Institue.mapper.UserMapper;
import com.institute.Institue.model.Organization;
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
        // 1. Fetch the Organization entity
        Organization org = organizationRepository.findById(UUID.fromString(orgId))
                .orElseThrow(() -> new RuntimeException("Organization not found"));

        // 2. Fetch the Role (TUTOR or STUDENT)
        String roleName = req.getRoles().split(",")[0].trim().toUpperCase();
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        // 3. Create User linked to that Organization
        User user = User.builder()
                .email(req.getEmail())
                .password(passwordEncoder.encode("defaultPassword")) // Set a default password or generate one
                .organization(org) // LINK THE ENTITY HERE
                .role(role)
                .enabled(true)
                .build();

        User saved = userRepository.save(user);
        return userMapper.toDto(saved);
    }
    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> listUsers(String orgId) {
        UUID orgUuid = UUID.fromString(orgId);

        // Use the method that FETCHES the organization and role entities
        List<User> users = userRepository.findByOrganizationIdWithRoles(orgUuid);

        return users.stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }
}