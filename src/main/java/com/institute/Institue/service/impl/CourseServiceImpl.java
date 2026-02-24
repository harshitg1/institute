package com.institute.Institue.service.impl;

import com.institute.Institue.dto.*;
import com.institute.Institue.exception.BadRequestException;
import com.institute.Institue.exception.ResourceNotFoundException;
import com.institute.Institue.model.Course;
import com.institute.Institue.model.Organization;
import com.institute.Institue.repository.CourseRepository;
import com.institute.Institue.repository.EnrollmentRepository;
import com.institute.Institue.repository.OrganizationRepository;
import com.institute.Institue.service.CourseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final OrganizationRepository organizationRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CourseDto> listAll() {
        return courseRepository.findAllPublished().stream()
                .map(this::toCourseDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public CourseDto createCourse(UUID orgId, CourseRequest request) {
        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", orgId));

        Course course = Course.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .price(request.getPrice())
                .thumbnailUrl(request.getThumbnailUrl())
                .durationHours(request.getDurationHours())
                .published(request.isPublished())
                .organization(org)
                .build();

        Course saved = courseRepository.save(course);
        log.info("Created course '{}' for organization {}", saved.getTitle(), orgId);
        return toCourseDto(saved);
    }

    @Transactional(readOnly = true)
    public List<CourseDto> listByOrganization(UUID orgId) {
        return courseRepository.findByOrganization_Id(orgId).stream()
                .map(this::toCourseDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CourseDto getCourse(UUID courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));
        return toCourseDto(course);
    }

    @Transactional
    public CourseDto updateCourse(UUID courseId, CourseRequest request) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));

        if (request.getTitle() != null)
            course.setTitle(request.getTitle());
        if (request.getDescription() != null)
            course.setDescription(request.getDescription());
        if (request.getPrice() != null)
            course.setPrice(request.getPrice());
        if (request.getThumbnailUrl() != null)
            course.setThumbnailUrl(request.getThumbnailUrl());
        if (request.getDurationHours() != null)
            course.setDurationHours(request.getDurationHours());
        course.setPublished(request.isPublished());

        Course saved = courseRepository.save(course);
        return toCourseDto(saved);
    }

    @Transactional
    public void deleteCourse(UUID courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));

        long enrollments = enrollmentRepository.countByCourse_Id(courseId);
        if (enrollments > 0) {
            throw new BadRequestException(
                    "Cannot delete course with " + enrollments + " active enrollments",
                    "COURSE_HAS_ENROLLMENTS");
        }

        courseRepository.delete(course);
        log.info("Deleted course '{}'", course.getTitle());
    }

    @Transactional(readOnly = true)
    public List<CourseDto> listPublishedCourses() {
        return courseRepository.findAllPublished().stream()
                .map(this::toCourseDto)
                .collect(Collectors.toList());
    }

    // --- Mapper ---
    private CourseDto toCourseDto(Course course) {
        long enrollmentCount = enrollmentRepository.countByCourse_Id(course.getId());
        return CourseDto.builder()
                .id(course.getId().toString())
                .title(course.getTitle())
                .description(course.getDescription())
                .price(course.getPrice())
                .thumbnailUrl(course.getThumbnailUrl())
                .durationHours(course.getDurationHours())
                .published(course.isPublished())
                .enrollmentCount(enrollmentCount)
                .createdAt(course.getCreatedAt())
                .build();
    }
}
