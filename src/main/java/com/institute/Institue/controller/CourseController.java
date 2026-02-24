package com.institute.Institue.controller;

import com.institute.Institue.dto.*;
import com.institute.Institue.model.User;
import com.institute.Institue.repository.EnrollmentRepository;
import com.institute.Institue.service.impl.CourseServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CourseController {

    private final CourseServiceImpl courseService;
    private final EnrollmentRepository enrollmentRepository;

    /**
     * Browse all published courses (any authenticated user)
     */
    @GetMapping("/courses")
    public ResponseEntity<ApiResponse<List<CourseDto>>> listPublished() {
        List<CourseDto> courses = courseService.listPublishedCourses();
        return ResponseEntity.ok(ApiResponse.success(courses));
    }

    /**
     * View course details (any authenticated user)
     */
    @GetMapping("/courses/{id}")
    public ResponseEntity<ApiResponse<CourseDto>> getCourse(@PathVariable java.util.UUID id) {
        CourseDto response = courseService.getCourse(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * View my enrolled/purchased courses (student only)
     */
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
}
