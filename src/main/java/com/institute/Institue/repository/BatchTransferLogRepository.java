package com.institute.Institue.repository;

import com.institute.Institue.model.BatchTransferLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BatchTransferLogRepository extends JpaRepository<BatchTransferLog, UUID> {

    List<BatchTransferLog> findByStudent_IdOrderByTransferredAtDesc(UUID studentId);
}
