package com.institute.Institue.repository;

import com.institute.Institue.model.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, UUID> {

    @Query("SELECT a FROM Attendance a " +
            "JOIN FETCH a.student " +
            "WHERE a.batch.id = :batchId AND a.date = :date " +
            "ORDER BY a.student.firstName")
    List<Attendance> findByBatchIdAndDate(@Param("batchId") UUID batchId, @Param("date") LocalDate date);

    @Query("SELECT a FROM Attendance a " +
            "JOIN FETCH a.batch " +
            "WHERE a.student.id = :studentId " +
            "ORDER BY a.date DESC")
    List<Attendance> findByStudentId(@Param("studentId") UUID studentId);

    @Query("SELECT a FROM Attendance a " +
            "JOIN FETCH a.student " +
            "WHERE a.batch.id = :batchId " +
            "ORDER BY a.date DESC, a.student.firstName")
    List<Attendance> findByBatchId(@Param("batchId") UUID batchId);

    boolean existsByBatch_IdAndStudent_IdAndDate(UUID batchId, UUID studentId, LocalDate date);
}
