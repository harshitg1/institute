package com.institute.Institue.controller;

import com.institute.Institue.model.Organization;
import com.institute.Institue.repository.OrganizationRepository;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/superadmin/organizations")
@RequiredArgsConstructor
public class SuperAdminController {

    private final OrganizationRepository organizationRepository;

    /**
     * Create a new organization
     */
    @PostMapping
    @Transactional
    public ResponseEntity<Organization> createOrganization(@RequestBody Organization org) {
        if ((org.getName() == null) || org.getName().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        if (organizationRepository.findByName(org.getName()).isPresent()) {
            return ResponseEntity.status(409).build(); // Conflict
        }

        Organization saved = organizationRepository.save(
                Organization.builder()
                        .name(org.getName())
                        .build()
        );
        return ResponseEntity.ok(saved);
    }

    /**
     * Get all organizations
     */
    @GetMapping
    public ResponseEntity<List<Organization>> getAllOrganizations() {
        List<Organization> organizations = organizationRepository.findAll();
        return ResponseEntity.ok(organizations);
    }

    /**
     * Get a single organization by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Organization> getOrganizationById(@PathVariable UUID id) {
        return organizationRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Update an organization name
     */
    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<Organization> updateOrganization(@PathVariable UUID id, @RequestBody Organization orgDetails) {
        return organizationRepository.findById(id)
                .map(existingOrg -> {
                    if (orgDetails.getName() != null && !orgDetails.getName().isBlank()) {
                        existingOrg.setName(orgDetails.getName());
                    }
                    return ResponseEntity.ok(organizationRepository.save(existingOrg));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Delete an organization
     * Warning: This usually cascades to users and courses depending on your DB constraints
     */
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> deleteOrganization(@PathVariable UUID id) {
        if (!organizationRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        organizationRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}