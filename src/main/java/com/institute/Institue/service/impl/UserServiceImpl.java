package com.institute.Institue.service.impl;

import com.institute.Institue.dto.CreateUserRequest;
import com.institute.Institue.dto.UserResponse;
import com.institute.Institue.model.User;
import com.institute.Institue.repository.UserRepository;
import com.institute.Institue.repository.OrganizationRepository;
import com.institute.Institue.service.UserService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;

    public UserServiceImpl(UserRepository userRepository, OrganizationRepository organizationRepository) {
        this.userRepository = userRepository;
        this.organizationRepository = organizationRepository;
    }

    @Override
    public UserResponse createUser(String orgId, CreateUserRequest req) {
        UUID orgUuid = orgId == null ? null : UUID.fromString(orgId);
        if (orgUuid != null && organizationRepository.findById(orgUuid).isEmpty()) return null;

        User user = User.builder()
                .username(req.getUsername())
                .email(req.getEmail())
                .organizationId(orgUuid)
                .roles(req.getRoles())
                .build();
        User saved = userRepository.save(user);
        return toResponse(saved);
    }

    @Override
    public List<UserResponse> listUsers(String orgId) {
        UUID orgUuid = orgId == null ? null : UUID.fromString(orgId);
        if (orgUuid == null) return java.util.Collections.emptyList();
        return userRepository.findByOrganizationId(orgUuid).stream().map(this::toResponse).collect(Collectors.toList());
    }

    private UserResponse toResponse(User u) {
        UserResponse r = new UserResponse();
        r.setId(u.getId() == null ? null : u.getId().toString());
        r.setUsername(u.getUsername());
        r.setEmail(u.getEmail());
        r.setOrganizationId(u.getOrganizationId() == null ? null : u.getOrganizationId().toString());
        r.setRoles(u.getRoles());
        return r;
    }
}

