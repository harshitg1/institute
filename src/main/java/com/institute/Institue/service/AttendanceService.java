package com.institute.Institue.service;

import com.institute.Institue.dto.AttendanceRequest;
import com.institute.Institue.dto.AttendanceResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface AttendanceService {

    AttendanceResponse markAttendance(UUID orgId, UUID batchId, AttendanceRequest request, UUID markedByUserId);

    AttendanceResponse getAttendanceByBatchAndDate(UUID orgId, UUID batchId, LocalDate date);

    List<AttendanceResponse.AttendanceRecordResponse> getAttendanceByStudent(UUID orgId, UUID studentId);

    AttendanceResponse getAttendanceByBatch(UUID orgId, UUID batchId);

    void updateAttendanceRecord(UUID orgId, UUID attendanceId, String status, String remarks);
}
