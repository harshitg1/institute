package com.institute.Institue.service.impl;

import com.institute.Institue.dto.*;
import com.institute.Institue.exception.BadRequestException;
import com.institute.Institue.exception.DuplicateResourceException;
import com.institute.Institue.exception.ResourceNotFoundException;
import com.institute.Institue.model.Batch;
import com.institute.Institue.model.BatchStudent;
import com.institute.Institue.model.Organization;
import com.institute.Institue.model.User;
import com.institute.Institue.repository.*;
import com.institute.Institue.service.BatchService;
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
public class BatchServiceImpl implements BatchService {

    private final BatchRepository batchRepository;
    private final BatchStudentRepository batchStudentRepository;
    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;

    @Override
    @Transactional
    public BatchResponse createBatch(UUID orgId, BatchRequest request) {
        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", orgId));

        if (batchRepository.existsByNameAndOrganization_Id(request.getName(), orgId)) {
            throw new DuplicateResourceException("Batch with name '" + request.getName() + "' already exists");
        }

        Batch batch = Batch.builder()
                .name(request.getName())
                .duration(request.getDuration())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .organization(org)
                .build();

        // Assign instructor if provided
        if (request.getInstructorId() != null && !request.getInstructorId().isBlank()) {
            User instructor = userRepository.findById(UUID.fromString(request.getInstructorId()))
                    .orElseThrow(() -> new ResourceNotFoundException("Instructor", "id", request.getInstructorId()));
            batch.setInstructor(instructor);
        }

        Batch saved = batchRepository.save(batch);
        log.info("Created batch '{}' in organization {}", saved.getName(), orgId);
        return toBatchResponse(saved, 0);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BatchResponse> listBatches(UUID orgId) {
        return batchRepository.findByOrganization_Id(orgId).stream()
                .map(batch -> {
                    long count = batchStudentRepository.countByBatch_IdAndActiveTrue(batch.getId());
                    return toBatchResponse(batch, count);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public BatchResponse getBatch(UUID batchId) {
        Batch batch = batchRepository.findByIdWithDetails(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("Batch", "id", batchId));
        long count = batchStudentRepository.countByBatch_IdAndActiveTrue(batchId);
        List<StudentResponse> students = getStudentsInBatch(batchId);
        return toBatchResponse(batch, count, students);
    }

    @Override
    @Transactional
    public BatchResponse updateBatch(UUID batchId, BatchRequest request) {
        Batch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("Batch", "id", batchId));

        if (request.getName() != null && !request.getName().isBlank()) {
            batch.setName(request.getName());
        }
        if (request.getDuration() != null)
            batch.setDuration(request.getDuration());
        if (request.getStartTime() != null)
            batch.setStartTime(request.getStartTime());
        if (request.getEndTime() != null)
            batch.setEndTime(request.getEndTime());

        if (request.getInstructorId() != null && !request.getInstructorId().isBlank()) {
            User instructor = userRepository.findById(UUID.fromString(request.getInstructorId()))
                    .orElseThrow(() -> new ResourceNotFoundException("Instructor", "id", request.getInstructorId()));
            batch.setInstructor(instructor);
        }

        Batch saved = batchRepository.save(batch);
        long count = batchStudentRepository.countByBatch_IdAndActiveTrue(batchId);
        return toBatchResponse(saved, count);
    }

    @Override
    @Transactional
    public void deleteBatch(UUID batchId) {
        Batch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("Batch", "id", batchId));

        long activeStudents = batchStudentRepository.countByBatch_IdAndActiveTrue(batchId);
        if (activeStudents > 0) {
            throw new BadRequestException(
                    "Cannot delete batch with " + activeStudents + " active students. Transfer them first.",
                    "BATCH_NOT_EMPTY");
        }

        batchRepository.delete(batch);
        log.info("Deleted batch '{}'", batch.getName());
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentResponse> getStudentsInBatch(UUID batchId) {
        Batch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("Batch", "id", batchId));

        List<BatchStudent> batchStudents = batchStudentRepository.findActiveByBatchId(batchId);
        return batchStudents.stream()
                .map(bs -> {
                    User student = bs.getStudent();
                    List<CourseDto> courses = enrollmentRepository.findByUserIdWithCourse(student.getId())
                            .stream()
                            .map(e -> CourseDto.builder()
                                    .id(e.getCourse().getId().toString())
                                    .title(e.getCourse().getTitle())
                                    .build())
                            .collect(Collectors.toList());

                    return StudentResponse.builder()
                            .id(student.getId().toString())
                            .email(student.getEmail())
                            .firstName(student.getFirstName())
                            .lastName(student.getLastName())
                            .status(student.getStudentStatus() != null ? student.getStudentStatus().name() : "ACTIVE")
                            .batchId(batchId.toString())
                            .batchName(batch.getName())
                            .courses(courses)
                            .createdAt(student.getCreatedAt())
                            .build();
                })
                .collect(Collectors.toList());
    }

    // --- Mapper ---
    private BatchResponse toBatchResponse(Batch batch, long studentCount) {
        return toBatchResponse(batch, studentCount, null);
    }

    private BatchResponse toBatchResponse(Batch batch, long studentCount, List<StudentResponse> students) {
        String instructorId = null;
        String instructorName = null;
        if (batch.getInstructor() != null) {
            instructorId = batch.getInstructor().getId().toString();
            String fn = batch.getInstructor().getFirstName();
            String ln = batch.getInstructor().getLastName();
            instructorName = ((fn != null ? fn : "") + " " + (ln != null ? ln : "")).trim();
            if (instructorName.isEmpty())
                instructorName = batch.getInstructor().getEmail();
        }

        return BatchResponse.builder()
                .id(batch.getId().toString())
                .name(batch.getName())
                .instructorId(instructorId)
                .instructorName(instructorName)
                .duration(batch.getDuration())
                .startTime(batch.getStartTime())
                .endTime(batch.getEndTime())
                .studentCount(studentCount)
                .active(batch.isActive())
                .createdAt(batch.getCreatedAt())
                .students(students)
                .build();
    }
}
