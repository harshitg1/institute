package com.institute.Institue.controller;

import com.institute.Institue.model.Organization;
import com.institute.Institue.repository.OrganizationRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/superadmin")
public class SuperAdminController {

    private final OrganizationRepository organizationRepository;

    public SuperAdminController(OrganizationRepository organizationRepository) {
        this.organizationRepository = organizationRepository;
    }

    @PostMapping("/organizations")
    public ResponseEntity<Organization> createOrganization(@RequestBody Organization org) {
        if (org.getName() == null || org.getName().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        if (organizationRepository.findByName(org.getName()).isPresent()) {
            return ResponseEntity.status(409).build();
        }
        Organization saved = organizationRepository.save(Organization.builder().name(org.getName()).build());
        return ResponseEntity.ok(saved);
    }
}

