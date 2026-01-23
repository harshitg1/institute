package com.institute.Institue.repository;

import com.institute.Institue.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    // switched to email-based lookup to match the updated User model (email is the principal)
    Optional<User> findByEmail(String email);
    List<User> findByOrganizationId(UUID organizationId);

    // Eagerly fetch roles for auth flows to avoid LazyInitializationException
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles WHERE u.email = :email")
    Optional<User> findByEmailWithRoles(@Param("email") String email);

    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.roles WHERE u.organizationId = :orgId")
    List<User> findByOrganizationIdWithRoles(@Param("orgId") UUID organizationId);
}
