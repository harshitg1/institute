package com.institute.Institue.controller;

import com.institute.Institue.dto.*;
import com.institute.Institue.model.User;
import com.institute.Institue.repository.UserRepository;
import com.institute.Institue.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/students")
@RequiredArgsConstructor
public class AdminStudentController {

    private final StudentService studentService;
    private final UserRepository userRepository;

    // Helper to resolve the admin principal when @AuthenticationPrincipal could be null
    private Optional<User> resolveAdmin(User admin) {
        if (admin != null) return Optional.of(admin);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return Optional.empty();

        Object p = auth.getPrincipal();
        if (p == null) return Optional.empty();

        // If principal is already our User entity
        if (p instanceof User) return Optional.of((User) p);

        // If principal is Spring Security UserDetails (e.g. org.springframework.security.core.userdetails.User)
        if (p instanceof UserDetails) {
            String username = ((UserDetails) p).getUsername();
            return userRepository.findByEmail(username);
        }

        // If principal is a String (username/email) or other object, try its string form
        String username = p instanceof String ? (String) p : p.toString();
        if (username == null || username.isBlank()) return Optional.empty();
        return userRepository.findByEmail(username);
    }

    /**
     * Add a new student (ACTIVE by default, assigned to a batch, optional courses)
     */
    @PostMapping
    public ResponseEntity<ApiResponse<StudentResponse>> createStudent(
            @AuthenticationPrincipal User admin,
            @Valid @RequestBody CreateStudentRequest request) {
        Optional<User> maybeAdmin = resolveAdmin(admin);
        if (maybeAdmin.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Unauthorized", "UNAUTHORIZED"));
        }

        UUID orgId = maybeAdmin.get().getOrganizationId();
        StudentResponse response = studentService.createStudent(orgId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    /**
     * List all students in the admin's organization
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<StudentResponse>>> listStudents(
            @AuthenticationPrincipal User admin) {
        Optional<User> maybeAdmin = resolveAdmin(admin);
        if (maybeAdmin.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Unauthorized", "UNAUTHORIZED"));
        }

        UUID orgId = maybeAdmin.get().getOrganizationId();
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
        Optional<User> maybeAdmin = resolveAdmin(admin);
        if (maybeAdmin.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Unauthorized", "UNAUTHORIZED"));
        }

        BatchTransferResponse response = studentService.transferBatch(id, request, maybeAdmin.get().getId());
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
        Optional<User> maybeAdmin = resolveAdmin(admin);
        if (maybeAdmin.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Unauthorized", "UNAUTHORIZED"));
        }

        StudentResponse response = studentService.assignCourses(id, request.getCourseIds(), maybeAdmin.get().getOrganizationId());
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
