package com.institute.Institue.controller;

import com.institute.Institue.dto.*;
import com.institute.Institue.model.User;
import com.institute.Institue.repository.EnrollmentRepository;
import com.institute.Institue.service.impl.CourseServiceImpl;
import com.institute.Institue.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseServiceImpl courseService;
    private final EnrollmentRepository enrollmentRepository;
    private final SecurityUtils securityUtils;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CourseDto>>> listPublished() {
        List<CourseDto> courses = courseService.listPublishedCourses();
        return ResponseEntity.ok(ApiResponse.success(courses));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CourseDto>> getCourse(@PathVariable UUID id) {
        CourseDto response = courseService.getCourse(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/student/my-courses")
    public ResponseEntity<ApiResponse<List<CourseDto>>> myCourses(
            @AuthenticationPrincipal User student) {
        List<CourseDto> courses = enrollmentRepository
                .findByUserIdWithCourse(student.getId())
                .stream()
                .map(e -> CourseDto.builder()
                        .id(e.getCourse().getId().toString())
                        .title(e.getCourse().getTitle())
                        .description(e.getCourse().getDescription())
                        .price(e.getCourse().getPrice())
                        .thumbnailUrl(e.getCourse().getThumbnailUrl())
                        .durationHours(e.getCourse().getDurationHours())
                        .build())
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(courses));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CourseDto>> createCourse(
            @AuthenticationPrincipal User admin,
            @Valid @RequestBody CourseRequest request) {
        Optional<User> maybeAdmin = securityUtils.resolveAdmin(admin);
        if (maybeAdmin.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Unauthorized", "UNAUTHORIZED"));
        }
        User resolved = maybeAdmin.get();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(courseService.createCourse(resolved.getOrganizationId(), request)));
    }

    @GetMapping("/admin")
    public ResponseEntity<ApiResponse<List<CourseDto>>> listCourses(
            @AuthenticationPrincipal User admin) {
        Optional<User> maybeAdmin = securityUtils.resolveAdmin(admin);
        if (maybeAdmin.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Unauthorized", "UNAUTHORIZED"));
        }
        User resolved = maybeAdmin.get();
        return ResponseEntity.ok(ApiResponse.success(courseService.listByOrganization(resolved.getOrganizationId())));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CourseDto>> updateCourse(
            @PathVariable UUID id,
            @Valid @RequestBody CourseRequest request) {
        return ResponseEntity.ok(ApiResponse.success(courseService.updateCourse(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCourse(@PathVariable UUID id) {
        courseService.deleteCourse(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Course deleted successfully"));
    }
}
