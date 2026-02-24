package com.institute.Institue.service;

import com.institute.Institue.dto.AttendanceRequest;
import com.institute.Institue.dto.AttendanceResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface AttendanceService {

    AttendanceResponse markAttendance(AttendanceRequest request, UUID markedByUserId);

    AttendanceResponse getAttendanceByBatchAndDate(UUID batchId, LocalDate date);

    List<AttendanceResponse.AttendanceRecordResponse> getAttendanceByStudent(UUID studentId);

    AttendanceResponse getAttendanceByBatch(UUID batchId);

    void updateAttendanceRecord(UUID attendanceId, String status, String remarks);
}
