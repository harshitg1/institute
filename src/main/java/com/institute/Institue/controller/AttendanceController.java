package com.institute.Institue.controller;

import com.institute.Institue.dto.*;
import com.institute.Institue.model.User;
import com.institute.Institue.service.AttendanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    /**
     * Mark attendance for a batch on a given date
     */
    @PostMapping
    public ResponseEntity<ApiResponse<AttendanceResponse>> markAttendance(
            @AuthenticationPrincipal User admin,
            @Valid @RequestBody AttendanceRequest request) {
        AttendanceResponse response = attendanceService.markAttendance(request, admin.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    /**
     * Get attendance records for a batch on a specific date
     */
    @GetMapping("/batch/{batchId}/date/{date}")
    public ResponseEntity<ApiResponse<AttendanceResponse>> getByBatchAndDate(
            @PathVariable UUID batchId,
            @PathVariable String date) {
        LocalDate parsedDate = LocalDate.parse(date);
        AttendanceResponse response = attendanceService.getAttendanceByBatchAndDate(batchId, parsedDate);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get all attendance records for a batch
     */
    @GetMapping("/batch/{batchId}")
    public ResponseEntity<ApiResponse<AttendanceResponse>> getByBatch(@PathVariable UUID batchId) {
        AttendanceResponse response = attendanceService.getAttendanceByBatch(batchId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get attendance history for a student
     */
    @GetMapping("/student/{studentId}")
    public ResponseEntity<ApiResponse<List<AttendanceResponse.AttendanceRecordResponse>>> getByStudent(
            @PathVariable UUID studentId) {
        List<AttendanceResponse.AttendanceRecordResponse> records = attendanceService.getAttendanceByStudent(studentId);
        return ResponseEntity.ok(ApiResponse.success(records));
    }

    /**
     * Update an attendance record
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> updateRecord(
            @PathVariable UUID id,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String remarks) {
        attendanceService.updateAttendanceRecord(id, status, remarks);
        return ResponseEntity.ok(ApiResponse.success(null, "Attendance record updated"));
    }
}
