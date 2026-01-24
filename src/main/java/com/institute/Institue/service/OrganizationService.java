package com.institute.Institue.service;

import com.institute.Institue.model.Organization;

import java.util.List;
import java.util.UUID;

public interface OrganizationService {
    Organization createOrganization(Organization org);
    List<Organization> getAllOrganizations();
    Organization getById(UUID id);
    Organization update(UUID id, Organization org);
    void delete(UUID id);

}