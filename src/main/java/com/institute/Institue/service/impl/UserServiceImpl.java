package com.institute.Institue.service.impl;

import com.institute.Institue.dto.CreateUserRequest;
import com.institute.Institue.dto.UserResponse;
import com.institute.Institue.mapper.UserMapper;
import com.institute.Institue.model.User;
import com.institute.Institue.model.Role;
import com.institute.Institue.repository.UserRepository;
import com.institute.Institue.repository.OrganizationRepository;
import com.institute.Institue.repository.RoleRepository;
import com.institute.Institue.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;

    public UserServiceImpl(UserRepository userRepository, OrganizationRepository organizationRepository, RoleRepository roleRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.organizationRepository = organizationRepository;
        this.roleRepository = roleRepository;
        this.userMapper = userMapper;
    }

    @Override
    @Transactional
    public UserResponse createUser(String orgId, CreateUserRequest req) {
        UUID orgUuid = orgId == null ? null : UUID.fromString(orgId);
        if (orgUuid != null && organizationRepository.findById(orgUuid).isEmpty()) return null;

        User user = User.builder()
                .email(req.getEmail())
                .organizationId(orgUuid)
                .build();

        // parse roles CSV and attach Role entities
        if (req.getRoles() != null && !req.getRoles().isBlank()) {
            String[] parts = req.getRoles().split(",");
            for (String p : parts) {
                String rn = p.trim();
                if (rn.isEmpty()) continue;
                Role r = roleRepository.findByName(rn).orElseGet(() -> roleRepository.save(Role.builder().id(UUID.randomUUID()).name(rn).build()));
                user.getRoles().add(r);
            }
        }

        User saved = userRepository.save(user);
        return userMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> listUsers(String orgId) {
        UUID orgUuid = orgId == null ? null : UUID.fromString(orgId);
        if (orgUuid == null) return java.util.Collections.emptyList();
        return userRepository.findByOrganizationIdWithRoles(orgUuid).stream().map(userMapper::toDto).collect(Collectors.toList());
    }
}
