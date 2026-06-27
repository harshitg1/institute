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
        String orgIdStr = TenantContext.getCurrentOrgId();
        if (orgIdStr == null) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        BatchResponse response = batchService.getBatch(UUID.fromString(orgIdStr), id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BatchResponse>> updateBatch(
            @PathVariable UUID id,
            @Valid @RequestBody BatchRequest request) {
        String orgIdStr = TenantContext.getCurrentOrgId();
        if (orgIdStr == null) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        BatchResponse response = batchService.updateBatch(UUID.fromString(orgIdStr), id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBatch(@PathVariable UUID id) {
        String orgIdStr = TenantContext.getCurrentOrgId();
        if (orgIdStr == null) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        batchService.deleteBatch(UUID.fromString(orgIdStr), id);
        return ResponseEntity.ok(ApiResponse.success(null, "Batch deleted successfully"));
    }

    @GetMapping("/{id}/students")
    public ResponseEntity<ApiResponse<List<StudentResponse>>> getStudentsInBatch(@PathVariable UUID id) {
        String orgIdStr = TenantContext.getCurrentOrgId();
        if (orgIdStr == null) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        List<StudentResponse> students = batchService.getStudentsInBatch(UUID.fromString(orgIdStr), id);
        return ResponseEntity.ok(ApiResponse.success(students));
    }

    @PostMapping("/{batchId}/attendance")
    public ResponseEntity<ApiResponse<AttendanceResponse>> markAttendance(
            @PathVariable UUID batchId,
            @AuthenticationPrincipal User admin,
            @Valid @RequestBody AttendanceRequest request) {
        String orgIdStr = TenantContext.getCurrentOrgId();
        if (orgIdStr == null) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        AttendanceResponse response = attendanceService.markAttendance(
                UUID.fromString(orgIdStr), batchId, request, admin != null ? admin.getId() : null);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping("/{batchId}/attendance/date/{date}")
    public ResponseEntity<ApiResponse<AttendanceResponse>> getAttendanceByDate(
            @PathVariable UUID batchId,
            @PathVariable String date) {
        String orgIdStr = TenantContext.getCurrentOrgId();
        if (orgIdStr == null) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        LocalDate parsedDate = LocalDate.parse(date);
        AttendanceResponse response = attendanceService.getAttendanceByBatchAndDate(UUID.fromString(orgIdStr), batchId, parsedDate);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{batchId}/attendance")
    public ResponseEntity<ApiResponse<AttendanceResponse>> getAttendanceByBatch(@PathVariable UUID batchId) {
        String orgIdStr = TenantContext.getCurrentOrgId();
        if (orgIdStr == null) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        AttendanceResponse response = attendanceService.getAttendanceByBatch(UUID.fromString(orgIdStr), batchId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/attendance/student/{studentId}")
    public ResponseEntity<ApiResponse<List<AttendanceResponse.AttendanceRecordResponse>>> getAttendanceByStudent(
            @PathVariable UUID studentId) {
        String orgIdStr = TenantContext.getCurrentOrgId();
        if (orgIdStr == null) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        List<AttendanceResponse.AttendanceRecordResponse> records = attendanceService.getAttendanceByStudent(UUID.fromString(orgIdStr), studentId);
        return ResponseEntity.ok(ApiResponse.success(records));
    }

    @PutMapping("/attendance/{id}")
    public ResponseEntity<ApiResponse<Void>> updateAttendanceRecord(
            @PathVariable UUID id,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String remarks) {
        String orgIdStr = TenantContext.getCurrentOrgId();
        if (orgIdStr == null) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        attendanceService.updateAttendanceRecord(UUID.fromString(orgIdStr), id, status, remarks);
        return ResponseEntity.ok(ApiResponse.success(null, "Attendance record updated"));
    }
}
