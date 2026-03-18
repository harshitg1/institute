package com.institute.Institue.service.impl;

import com.institute.Institue.model.Organization;
import com.institute.Institue.repository.OrganizationRepository;
import com.institute.Institue.service.OrganizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.institute.Institue.dto.OrganizationCreateRequest;
import com.institute.Institue.model.User;
import com.institute.Institue.model.Role;
import com.institute.Institue.repository.UserRepository;
import com.institute.Institue.repository.RoleRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrganizationServiceImpl implements OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public Organization createOrganization(Organization org) {
        if (org.getName() == null || org.getName().isBlank()) {
            throw new RuntimeException("Organization name is required");
        }
        if (organizationRepository.findByName(org.getName()).isPresent()) {
            throw new RuntimeException("Organization with this name already exists");
        }
        return organizationRepository.save(org);
    }

    @Override
    @Transactional
    public Organization createOrganizationWithAdmin(OrganizationCreateRequest request) {
        if (request.getName() == null || request.getName().isBlank()) {
            throw new RuntimeException("Organization name is required");
        }
        if (organizationRepository.findByName(request.getName()).isPresent()) {
            throw new RuntimeException("Organization with this name already exists");
        }
        if (userRepository.existsByEmail(request.getAdminEmail())) {
            throw new RuntimeException("User with this email already exists");
        }

        Organization org = Organization.builder()
                .name(request.getName())
                .active(true)
                .build();
        Organization savedOrg = organizationRepository.save(org);

        Role adminRole = roleRepository.findByRole(com.institute.Institue.model.enums.UserRole.ORG_ADMIN)
                .orElseThrow(() -> new RuntimeException("ADMIN role not found"));

        User adminUser = User.builder()
                .email(request.getAdminEmail())
                .password(passwordEncoder.encode(request.getAdminPassword()))
                .firstName(request.getAdminFirstName())
                .lastName(request.getAdminLastName())
                .organization(savedOrg)
                .role(adminRole)
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .build();
        userRepository.save(adminUser);

        return savedOrg;
    }

    @Override
    public List<Organization> getAllOrganizations() {
        return organizationRepository.findAll();
    }

    @Override
    public Page<Organization> getAllOrganizations(Pageable pageable) {
        return organizationRepository.findAll(pageable);
    }

    @Override
    public Organization getById(UUID id) {
        return organizationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Organization not found"));
    }

    @Override
    @Transactional
    public Organization update(UUID id, Organization details) {
        Organization existing = getById(id);
        existing.setName(details.getName());
        return organizationRepository.save(existing);
    }

    @Override
    @Transactional
    public Organization changeOrganizationStatus(UUID id, boolean active) {
        Organization existing = getById(id);
        existing.setActive(active);
        return organizationRepository.save(existing);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        if (!organizationRepository.existsById(id)) {
            throw new RuntimeException("Organization not found");
        }
        organizationRepository.deleteById(id);
    }
}