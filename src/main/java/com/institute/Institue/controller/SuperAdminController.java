package com.institute.Institue.controller;

import com.institute.Institue.dto.UserResponse;
import com.institute.Institue.model.Organization;
import com.institute.Institue.repository.OrganizationRepository;
import com.institute.Institue.dto.OrganizationCreateRequest;
import com.institute.Institue.model.Role;
import com.institute.Institue.model.enums.UserRole;
import com.institute.Institue.repository.RoleRepository;
import com.institute.Institue.repository.UserRepository;
import com.institute.Institue.service.OrganizationService;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/superadmin/organizations")
@RequiredArgsConstructor
public class SuperAdminController {

    private final OrganizationService organizationService;
    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    /**
     * Create a new organization
     */
    @PostMapping
    public ResponseEntity<Organization> createOrganization(@RequestBody OrganizationCreateRequest request) {
        Organization saved = organizationService.createOrganizationWithAdmin(request);
        return ResponseEntity.ok(saved);
    }

    /**
     * Get all organizations (paginated)
     */
    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<Page<Organization>> getAllOrganizations(@PageableDefault(size = 10) Pageable pageable) {
        Page<Organization> organizations = organizationService.getAllOrganizations(pageable);
        return ResponseEntity.ok(organizations);
    }

    /**
     * Get all admins across the platform (paginated)
     */
    @GetMapping("/admins")
    @Transactional(readOnly = true)
    @Secured("SUPER_ADMIN")
    public ResponseEntity<Page<UserResponse>> getAllAdmins(@PageableDefault(size = 10) Pageable pageable) {
        Role adminRole = roleRepository.findByRole(UserRole.ORG_ADMIN)
                .orElseThrow(() -> new RuntimeException("Admin role not found"));
        Page<UserResponse> admins = userRepository.findByRole_Id(adminRole.getId(), pageable)
                .map(user -> new UserResponse(
                        user.getId(),
                        user.getEmail(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getRole() != null ? user.getRole().getRole().name() : null,
                        user.getOrganization() != null ? user.getOrganization().getId() : null,
                        user.getOrganization() != null ? user.getOrganization().getName() : null
                ));
        return ResponseEntity.ok(admins);
    }

    /**
     * Get a single organization by ID
     */
    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<Organization> getOrganizationById( @PathVariable UUID id) {
        return organizationRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Update an organization name
     */
    @PutMapping("/{id}")
    public ResponseEntity<Organization> updateOrganization(@PathVariable UUID id, @RequestBody Organization orgDetails) {
        return ResponseEntity.ok(organizationService.update(id, orgDetails));
    }

    /**
     * Activate an organization
     */
    @PutMapping("/{id}/activate")
    public ResponseEntity<Organization> activateOrganization(@PathVariable UUID id) {
        return ResponseEntity.ok(organizationService.changeOrganizationStatus(id, true));
    }

    /**
     * Deactivate an organization
     */
    @PutMapping("/{id}/deactivate")
    public ResponseEntity<Organization> deactivateOrganization(@PathVariable UUID id) {
        return ResponseEntity.ok(organizationService.changeOrganizationStatus(id, false));
    }

    /**
     * Delete an organization
     * Warning: This usually cascades to users and courses depending on your DB constraints
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrganization(@PathVariable UUID id) {
        organizationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}