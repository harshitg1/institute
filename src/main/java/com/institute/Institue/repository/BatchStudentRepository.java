package com.institute.Institue.repository;

import com.institute.Institue.model.BatchStudent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BatchStudentRepository extends JpaRepository<BatchStudent, UUID> {

    @Query("SELECT bs FROM BatchStudent bs " +
            "JOIN FETCH bs.student " +
            "WHERE bs.batch.id = :batchId AND bs.active = true")
    List<BatchStudent> findActiveByBatchId(@Param("batchId") UUID batchId);

    @Query("SELECT bs FROM BatchStudent bs " +
            "JOIN FETCH bs.batch " +
            "WHERE bs.student.id = :studentId AND bs.active = true")
    Optional<BatchStudent> findActiveByStudentId(@Param("studentId") UUID studentId);

    @Query("SELECT bs FROM BatchStudent bs " +
            "WHERE bs.student.id = :studentId AND bs.batch.id = :batchId AND bs.active = true")
    Optional<BatchStudent> findActiveByStudentIdAndBatchId(
            @Param("studentId") UUID studentId, @Param("batchId") UUID batchId);

    long countByBatch_IdAndActiveTrue(UUID batchId);

    boolean existsByStudent_IdAndBatch_IdAndActiveTrue(UUID studentId, UUID batchId);
}
