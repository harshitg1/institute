package com.institute.Institue.controller;

import com.institute.Institue.dto.*;
import com.institute.Institue.model.User;
import com.institute.Institue.service.StudentService;
import com.institute.Institue.tenant.TenantContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/students")
@RequiredArgsConstructor
public class AdminStudentController {

    private final StudentService studentService;

    /**
     * Add a new student (ACTIVE by default, assigned to a batch, optional courses)
     */
    @PostMapping
    public ResponseEntity<ApiResponse<StudentResponse>> createStudent(
            @AuthenticationPrincipal User admin,
            @Valid @RequestBody CreateStudentRequest request) {
        String orgIdStr = TenantContext.getCurrentOrgId();
        if (orgIdStr == null) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        UUID orgId = UUID.fromString(orgIdStr);
        StudentResponse response = studentService.createStudent(orgId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    /**
     * List all students in the admin's organization
     */
    @GetMapping
    @Secured("Org_Admin")
    public ResponseEntity<ApiResponse<List<StudentResponse>>> listStudents() {
        String orgIdStr = TenantContext.getCurrentOrgId();
        if (orgIdStr == null) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        UUID orgId = UUID.fromString(orgIdStr);
        List<StudentResponse> students = studentService.listStudents(orgId);
        return ResponseEntity.ok(ApiResponse.success(students));
    }

    /**
     * Get student details by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StudentResponse>> getStudent(@PathVariable UUID id) {
        StudentResponse response = studentService.getStudent(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Update student details
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<StudentResponse>> updateStudent(
            @PathVariable UUID id,
            @RequestBody CreateStudentRequest request) {
        StudentResponse response = studentService.updateStudent(id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Deactivate/remove a student
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deactivateStudent(@PathVariable UUID id) {
        studentService.deactivateStudent(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Student deactivated successfully"));
    }

    /**
     * Toggle student status (ACTIVE, INACTIVE, SUSPENDED, GRADUATED)
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<StudentResponse>> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody StudentStatusRequest request) {
        StudentResponse response = studentService.updateStudentStatus(id, request.getStatus());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Transfer student to a different batch
     */
    @PostMapping("/{id}/transfer")
    public ResponseEntity<ApiResponse<BatchTransferResponse>> transferBatch(
            @PathVariable UUID id,
            @AuthenticationPrincipal User admin,
            @Valid @RequestBody BatchTransferRequest request) {
        BatchTransferResponse response = studentService.transferBatch(id, request, admin.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Assign courses to a student
     */
    @PostMapping("/{id}/courses")
    public ResponseEntity<ApiResponse<StudentResponse>> assignCourses(
            @PathVariable UUID id,
            @AuthenticationPrincipal User admin,
            @Valid @RequestBody AssignCoursesRequest request) {
        String orgIdStr = TenantContext.getCurrentOrgId();
        if (orgIdStr == null) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        UUID orgId = UUID.fromString(orgIdStr);
        StudentResponse response = studentService.assignCourses(id, request.getCourseIds(), orgId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Remove a course from a student
     */
    @DeleteMapping("/{studentId}/courses/{courseId}")
    public ResponseEntity<ApiResponse<Void>> removeCourse(
            @PathVariable UUID studentId,
            @PathVariable UUID courseId) {
        studentService.removeCourseFromStudent(studentId, courseId);
        return ResponseEntity.ok(ApiResponse.success(null, "Course removed from student"));
    }
}
