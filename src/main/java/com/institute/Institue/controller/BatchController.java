package com.institute.Institue.controller;

import com.institute.Institue.dto.*;
import com.institute.Institue.model.User;
import com.institute.Institue.service.AttendanceService;
import com.institute.Institue.service.BatchService;
import com.institute.Institue.tenant.TenantContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/batches")
@RequiredArgsConstructor
@Slf4j
public class BatchController {

    private final BatchService batchService;
    private final AttendanceService attendanceService;

    // ==================== BATCH ENDPOINTS ====================

    @PostMapping
    public ResponseEntity<ApiResponse<BatchResponse>> createBatch(
            @AuthenticationPrincipal User admin,
            @Valid @RequestBody BatchRequest request) {
        String orgIdStr = TenantContext.getCurrentOrgId();
        if (orgIdStr == null) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        UUID orgId = UUID.fromString(orgIdStr);
        BatchResponse response = batchService.createBatch(orgId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<BatchResponse>>> listBatches() {
        String orgIdStr = TenantContext.getCurrentOrgId();
        if (orgIdStr == null) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        UUID orgId = UUID.fromString(orgIdStr);
        List<BatchResponse> batches = batchService.listBatches(orgId);
        return ResponseEntity.ok(ApiResponse.success(batches));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BatchResponse>> getBatch(@PathVariable UUID id) {
        BatchResponse response = batchService.getBatch(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BatchResponse>> updateBatch(
            @PathVariable UUID id,
            @Valid @RequestBody BatchRequest request) {
        BatchResponse response = batchService.updateBatch(id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBatch(@PathVariable UUID id) {
        batchService.deleteBatch(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Batch deleted successfully"));
    }

    @GetMapping("/{id}/students")
    public ResponseEntity<ApiResponse<List<StudentResponse>>> getStudentsInBatch(@PathVariable UUID id) {
        List<StudentResponse> students = batchService.getStudentsInBatch(id);
        return ResponseEntity.ok(ApiResponse.success(students));
    }

    // ==================== ATTENDANCE ENDPOINTS ====================

    @PostMapping("/{batchId}/attendance")
    public ResponseEntity<ApiResponse<AttendanceResponse>> markAttendance(
            @PathVariable UUID batchId,
            @Valid @RequestBody AttendanceRequest request) {
        AttendanceResponse response = attendanceService.markAttendance(request, batchId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping("/{batchId}/attendance/date/{date}")
    public ResponseEntity<ApiResponse<AttendanceResponse>> getAttendanceByDate(
            @PathVariable UUID batchId,
            @PathVariable String date) {
        LocalDate parsedDate = LocalDate.parse(date);
        AttendanceResponse response = attendanceService.getAttendanceByBatchAndDate(batchId, parsedDate);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{batchId}/attendance")
    public ResponseEntity<ApiResponse<AttendanceResponse>> getAttendanceByBatch(@PathVariable UUID batchId) {
        AttendanceResponse response = attendanceService.getAttendanceByBatch(batchId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/attendance/student/{studentId}")
    public ResponseEntity<ApiResponse<List<AttendanceResponse.AttendanceRecordResponse>>> getAttendanceByStudent(
            @PathVariable UUID studentId) {
        List<AttendanceResponse.AttendanceRecordResponse> records = attendanceService.getAttendanceByStudent(studentId);
        return ResponseEntity.ok(ApiResponse.success(records));
    }

    @PutMapping("/attendance/{id}")
    public ResponseEntity<ApiResponse<Void>> updateAttendanceRecord(
            @PathVariable UUID id,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String remarks) {
        attendanceService.updateAttendanceRecord(id, status, remarks);
        return ResponseEntity.ok(ApiResponse.success(null, "Attendance record updated"));
    }
}
