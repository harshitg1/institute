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

    @Query("SELECT e FROM Enrollment e " +
            "JOIN FETCH e.course " +
            "WHERE e.user.id IN :userIds")
    List<Enrollment> findByUserIdsWithCourse(@Param("userIds") List<UUID> userIds);

    boolean existsByUser_IdAndCourse_Id(UUID userId, UUID courseId);

    Optional<Enrollment> findByUser_IdAndCourse_Id(UUID userId, UUID courseId);

    long countByCourse_Id(UUID courseId);

    @Query("SELECT e FROM Enrollment e " +
            "JOIN FETCH e.course " +
            "WHERE e.organization.id = :orgId")
    List<Enrollment> findByOrganizationId(@Param("orgId") UUID orgId);

    @Query("SELECT e.course.id, COUNT(e) FROM Enrollment e " +
            "WHERE e.course.id IN :courseIds " +
            "GROUP BY e.course.id")
    List<Object[]> countByCourseIds(@Param("courseIds") List<UUID> courseIds);
}
