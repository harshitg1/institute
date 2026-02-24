package com.institute.Institue.repository;

import com.institute.Institue.model.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, UUID> {

    @Query("SELECT e FROM Enrollment e " +
            "JOIN FETCH e.course " +
            "WHERE e.user.id = :userId")
    List<Enrollment> findByUserIdWithCourse(@Param("userId") UUID userId);

    boolean existsByUser_IdAndCourse_Id(UUID userId, UUID courseId);

    Optional<Enrollment> findByUser_IdAndCourse_Id(UUID userId, UUID courseId);

    long countByCourse_Id(UUID courseId);

    @Query("SELECT e FROM Enrollment e " +
            "JOIN FETCH e.course " +
            "WHERE e.organization.id = :orgId")
    List<Enrollment> findByOrganizationId(@Param("orgId") UUID orgId);
}
