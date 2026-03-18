package com.institute.Institue.service.impl;

import com.institute.Institue.dto.*;
import com.institute.Institue.exception.BadRequestException;
import com.institute.Institue.exception.DuplicateResourceException;
import com.institute.Institue.exception.ResourceNotFoundException;
import com.institute.Institue.model.*;
import com.institute.Institue.model.enums.StudentStatus;
import com.institute.Institue.repository.*;
import com.institute.Institue.service.StudentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentServiceImpl implements StudentService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final OrganizationRepository organizationRepository;
    private final BatchRepository batchRepository;
    private final BatchStudentRepository batchStudentRepository;
    private final BatchTransferLogRepository transferLogRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public StudentResponse createStudent(UUID orgId, CreateStudentRequest request) {
        // Validate organization
        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", orgId));

        // Check duplicate email
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("User with email '" + request.getEmail() + "' already exists");
        }

        // Validate batch exists
        Batch batch = batchRepository.findById(UUID.fromString(request.getBatchId()))
                .orElseThrow(() -> new ResourceNotFoundException("Batch", "id", request.getBatchId()));

        // Get STUDENT role
        Role studentRole = roleRepository.findByName("STUDENT")
                .orElseThrow(() -> new ResourceNotFoundException("Role", "name", "STUDENT"));

        // Create user
        User student = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode("defaultPassword123"))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .organization(org)
                .role(studentRole)
                .studentStatus(StudentStatus.ACTIVE)
                .enabled(true)
                .build();

        User saved = userRepository.save(student);

        // Add to batch
        BatchStudent batchStudent = BatchStudent.builder()
                .batch(batch)
                .student(saved)
                .active(true)
                .build();
        batchStudentRepository.save(batchStudent);

        // Assign courses if provided
        List<CourseDto> assignedCourses = List.of();
        if (request.getCourseIds() != null && !request.getCourseIds().isEmpty()) {
            assignedCourses = assignCoursesInternal(saved, request.getCourseIds(), org);
        }

        log.info("Created student '{}' in batch '{}' with {} courses",
                saved.getEmail(), batch.getName(), assignedCourses.size());

        return toStudentResponse(saved, batch.getId().toString(), batch.getName(), assignedCourses);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentResponse> listStudents(UUID orgId) {
        List<User> students = userRepository.findByOrganizationIdWithRoles(orgId).stream()
                .filter(u -> "STUDENT".equals(u.getRole().getRole().name()))
                .collect(Collectors.toList());

        return students.stream().map(student -> {
            // Find active batch
            var activeBatch = batchStudentRepository.findActiveByStudentId(student.getId());
            String batchId = activeBatch.map(bs -> bs.getBatch().getId().toString()).orElse(null);
            String batchName = activeBatch.map(bs -> bs.getBatch().getName()).orElse(null);

            // Get courses
            List<CourseDto> courses = enrollmentRepository.findByUserIdWithCourse(student.getId())
                    .stream()
                    .map(e -> CourseDto.builder()
                            .id(e.getCourse().getId().toString())
                            .title(e.getCourse().getTitle())
                            .build())
                    .collect(Collectors.toList());

            return toStudentResponse(student, batchId, batchName, courses);
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public StudentResponse getStudent(UUID studentId) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", studentId));

        var activeBatch = batchStudentRepository.findActiveByStudentId(studentId);
        String batchId = activeBatch.map(bs -> bs.getBatch().getId().toString()).orElse(null);
        String batchName = activeBatch.map(bs -> bs.getBatch().getName()).orElse(null);

        List<CourseDto> courses = enrollmentRepository.findByUserIdWithCourse(studentId)
                .stream()
                .map(e -> CourseDto.builder()
                        .id(e.getCourse().getId().toString())
                        .title(e.getCourse().getTitle())
                        .price(e.getCourse().getPrice())
                        .build())
                .collect(Collectors.toList());

        return toStudentResponse(student, batchId, batchName, courses);
    }

    @Override
    @Transactional
    public StudentResponse updateStudent(UUID studentId, CreateStudentRequest request) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", studentId));

        if (request.getFirstName() != null)
            student.setFirstName(request.getFirstName());
        if (request.getLastName() != null)
            student.setLastName(request.getLastName());
        // Email change needs duplicate check
        if (request.getEmail() != null && !request.getEmail().equals(student.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new DuplicateResourceException("Email '" + request.getEmail() + "' is already taken");
            }
            student.setEmail(request.getEmail());
        }

        userRepository.save(student);
        return getStudent(studentId);
    }

    @Override
    @Transactional
    public void deactivateStudent(UUID studentId) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", studentId));

        student.setStudentStatus(StudentStatus.INACTIVE);
        student.setEnabled(false);
        userRepository.save(student);
        log.info("Deactivated student '{}'", student.getEmail());
    }

    @Override
    @Transactional
    public StudentResponse updateStudentStatus(UUID studentId, String status) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", studentId));

        try {
            StudentStatus newStatus = StudentStatus.valueOf(status.toUpperCase());
            student.setStudentStatus(newStatus);

            // If status is ACTIVE, enable the account; otherwise disable
            student.setEnabled(newStatus == StudentStatus.ACTIVE);

            userRepository.save(student);
            log.info("Updated student '{}' status to {}", student.getEmail(), newStatus);
            return getStudent(studentId);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid status: " + status +
                    ". Valid values: ACTIVE, INACTIVE, SUSPENDED, GRADUATED");
        }
    }

    @Override
    @Transactional
    public BatchTransferResponse transferBatch(UUID studentId, BatchTransferRequest request, UUID transferredByUserId) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", studentId));

        UUID targetBatchId = UUID.fromString(request.getTargetBatchId());
        Batch targetBatch = batchRepository.findById(targetBatchId)
                .orElseThrow(() -> new ResourceNotFoundException("Batch", "id", request.getTargetBatchId()));

        // Find current active batch
        BatchStudent currentBs = batchStudentRepository.findActiveByStudentId(studentId)
                .orElseThrow(() -> new BadRequestException("Student is not assigned to any batch"));

        Batch fromBatch = currentBs.getBatch();

        if (fromBatch.getId().equals(targetBatchId)) {
            throw new BadRequestException("Student is already in this batch");
        }

        // Deactivate current batch membership
        currentBs.setActive(false);
        currentBs.setLeftAt(Instant.now());
        batchStudentRepository.save(currentBs);

        // Create new batch membership
        BatchStudent newBs = BatchStudent.builder()
                .batch(targetBatch)
                .student(student)
                .active(true)
                .build();
        batchStudentRepository.save(newBs);

        // Log the transfer
        User transferredBy = null;
        if (transferredByUserId != null) {
            transferredBy = userRepository.findById(transferredByUserId).orElse(null);
        }

        BatchTransferLog transferLog = BatchTransferLog.builder()
                .student(student)
                .fromBatch(fromBatch)
                .toBatch(targetBatch)
                .reason(request.getReason())
                .transferredBy(transferredBy)
                .build();
        transferLogRepository.save(transferLog);

        log.info("Transferred student '{}' from batch '{}' to '{}'",
                student.getEmail(), fromBatch.getName(), targetBatch.getName());

        String studentName = ((student.getFirstName() != null ? student.getFirstName() : "") + " " +
                (student.getLastName() != null ? student.getLastName() : "")).trim();

        return BatchTransferResponse.builder()
                .studentId(studentId.toString())
                .studentName(studentName.isEmpty() ? student.getEmail() : studentName)
                .previousBatchId(fromBatch.getId().toString())
                .previousBatchName(fromBatch.getName())
                .newBatchId(targetBatch.getId().toString())
                .newBatchName(targetBatch.getName())
                .reason(request.getReason())
                .transferredAt(transferLog.getTransferredAt())
                .build();
    }

    @Override
    @Transactional
    public StudentResponse assignCourses(UUID studentId, List<String> courseIds, UUID orgId) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", studentId));

        Organization org = student.getOrganization();
        assignCoursesInternal(student, courseIds, org);

        return getStudent(studentId);
    }

    @Override
    @Transactional
    public void removeCourseFromStudent(UUID studentId, UUID courseId) {
        Enrollment enrollment = enrollmentRepository.findByUser_IdAndCourse_Id(studentId, courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment", "studentId+courseId",
                        studentId + "+" + courseId));

        enrollmentRepository.delete(enrollment);
        log.info("Removed course {} from student {}", courseId, studentId);
    }

    // --- Internal helpers ---

    private List<CourseDto> assignCoursesInternal(User student, List<String> courseIds, Organization org) {
        return courseIds.stream().map(courseIdStr -> {
            UUID courseId = UUID.fromString(courseIdStr);
            Course course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseIdStr));

            // Skip if already enrolled
            if (enrollmentRepository.existsByUser_IdAndCourse_Id(student.getId(), courseId)) {
                return CourseDto.builder()
                        .id(course.getId().toString())
                        .title(course.getTitle())
                        .build();
            }

            Enrollment enrollment = Enrollment.builder()
                    .user(student)
                    .course(course)
                    .organization(org)
                    .purchased(false)
                    .build();
            enrollmentRepository.save(enrollment);

            return CourseDto.builder()
                    .id(course.getId().toString())
                    .title(course.getTitle())
                    .build();
        }).collect(Collectors.toList());
    }

    private StudentResponse toStudentResponse(User student, String batchId, String batchName, List<CourseDto> courses) {
        return StudentResponse.builder()
                .id(student.getId().toString())
                .email(student.getEmail())
                .firstName(student.getFirstName())
                .lastName(student.getLastName())
                .status(student.getStudentStatus() != null ? student.getStudentStatus().name() : "ACTIVE")
                .batchId(batchId)
                .batchName(batchName)
                .courses(courses)
                .createdAt(student.getCreatedAt())
                .build();
    }
}
