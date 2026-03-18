package com.institute.Institue.service;

import com.institute.Institue.model.Organization;
import com.institute.Institue.dto.OrganizationCreateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface OrganizationService {
    Organization createOrganization(Organization org);
    Organization createOrganizationWithAdmin(OrganizationCreateRequest request);
    List<Organization> getAllOrganizations();
    Page<Organization> getAllOrganizations(Pageable pageable);
    Organization getById(UUID id);
    Organization update(UUID id, Organization org);
    Organization changeOrganizationStatus(UUID id, boolean active);
    void delete(UUID id);

}