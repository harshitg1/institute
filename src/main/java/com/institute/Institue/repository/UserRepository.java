package com.institute.Institue.repository;

import com.institute.Institue.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    // Fixed: Spring Data JPA automatically handles the underscore for nested properties
    List<User> findByOrganization_Id(UUID organizationId);

    boolean existsByEmail(String email);

    // Fixed: References the single 'role' entity
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.role WHERE u.email = :email")
    Optional<User> findByEmailWithRoles(@Param("email") String email);

    // Fixed: Changed u.organizationId to u.organization.id
    @Query("SELECT DISTINCT u FROM User u " +
            "LEFT JOIN FETCH u.role " +
            "LEFT JOIN FETCH u.organization " +
            "WHERE u.organization.id = :orgId")
    List<User> findByOrganizationIdWithRoles(@Param("orgId") UUID organizationId);

    Page<User> findByRole_Name(String roleName, Pageable pageable);

    Page<User> findByRole_Id(UUID roleId, Pageable pageable);
}
