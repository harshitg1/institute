package com.institute.Institue.repository;

import com.institute.Institue.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CourseRepository extends JpaRepository<Course, UUID> {

    List<Course> findByOrganization_Id(UUID organizationId);

    @Query("SELECT c FROM Course c WHERE c.organization.id = :orgId AND c.published = true")
    List<Course> findPublishedByOrganizationId(@Param("orgId") UUID orgId);

    @Query("SELECT c FROM Course c WHERE c.published = true ORDER BY c.createdAt DESC")
    List<Course> findAllPublished();

    Optional<Course> findByIdAndOrganization_Id(UUID id, UUID organizationId);
}
