package com.institute.Institue.controller;

import com.institute.Institue.dto.*;
import com.institute.Institue.model.User;
import com.institute.Institue.service.impl.CourseServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/courses")
@RequiredArgsConstructor
public class AdminCourseController {

    private final CourseServiceImpl courseService;

    /**
     * Create a new course
     */
    @PostMapping
    public ResponseEntity<ApiResponse<CourseDto>> createCourse(
            @AuthenticationPrincipal User admin,
            @Valid @RequestBody CourseRequest request) {
        UUID orgId = admin.getOrganizationId();
        CourseDto response = courseService.createCourse(orgId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    /**
     * List all courses in the organization (published + unpublished)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<CourseDto>>> listCourses(
            @AuthenticationPrincipal User admin) {
        UUID orgId = admin.getOrganizationId();
        List<CourseDto> courses = courseService.listByOrganization(orgId);
        return ResponseEntity.ok(ApiResponse.success(courses));
    }

    /**
     * Get course details
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CourseDto>> getCourse(@PathVariable UUID id) {
        CourseDto response = courseService.getCourse(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Update course details
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CourseDto>> updateCourse(
            @PathVariable UUID id,
            @Valid @RequestBody CourseRequest request) {
        CourseDto response = courseService.updateCourse(id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Delete a course (only if no enrollments)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCourse(@PathVariable UUID id) {
        courseService.deleteCourse(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Course deleted successfully"));
    }
}
