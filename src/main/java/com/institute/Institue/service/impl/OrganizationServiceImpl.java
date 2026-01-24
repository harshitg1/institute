package com.institute.Institue.service.impl;

import com.institute.Institue.model.Organization;
import com.institute.Institue.repository.OrganizationRepository;
import com.institute.Institue.service.OrganizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrganizationServiceImpl implements OrganizationService {

    private final OrganizationRepository organizationRepository;

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
    public List<Organization> getAllOrganizations() {
        return organizationRepository.findAll();
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
    public void delete(UUID id) {
        if (!organizationRepository.existsById(id)) {
            throw new RuntimeException("Organization not found");
        }
        organizationRepository.deleteById(id);
    }
}