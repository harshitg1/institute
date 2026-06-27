package com.institute.Institue.service.impl;

import com.institute.Institue.dto.*;
import com.institute.Institue.exception.BadRequestException;
import com.institute.Institue.exception.DuplicateResourceException;
import com.institute.Institue.exception.ResourceNotFoundException;
import com.institute.Institue.mapper.BatchMapper;
import com.institute.Institue.mapper.CourseMapper;
import com.institute.Institue.mapper.StudentMapper;
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
import java.util.Map;
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
    private final BatchMapper batchMapper;
    private final StudentMapper studentMapper;
    private final CourseMapper courseMapper;

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
            if (instructor.getOrganizationId() == null || !instructor.getOrganizationId().equals(orgId)) {
                throw new BadRequestException("Instructor does not belong to this organization", "INSTRUCTOR_OUTSIDE_ORG");
            }
            batch.setInstructor(instructor);
        }

        Batch saved = batchRepository.save(batch);
        log.info("Created batch '{}' in organization {}", saved.getName(), orgId);
        return batchMapper.toDto(saved, 0L, List.of());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BatchResponse> listBatches(UUID orgId) {
        List<Batch> batches = batchRepository.findByOrganization_Id(orgId);
        Map<UUID, Long> studentCounts = batchStudentRepository.countActiveByBatchIds(
                        batches.stream().map(Batch::getId).toList())
                .stream()
                .collect(Collectors.toMap(
                        row -> (UUID) row[0],
                        row -> (Long) row[1]
                ));

        return batches.stream()
                .map(batch -> {
                    long count = studentCounts.getOrDefault(batch.getId(), 0L);
                    return batchMapper.toDto(batch, count, List.of());
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public BatchResponse getBatch(UUID orgId, UUID batchId) {
        Batch batch = batchRepository.findByIdWithDetailsAndOrganizationId(batchId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Batch", "id", batchId));
        long count = batchStudentRepository.countByBatch_IdAndActiveTrue(batchId);
        List<StudentResponse> students = getStudentsInBatch(orgId, batchId);
        return batchMapper.toDto(batch, count, students);
    }

    @Override
    @Transactional
    public BatchResponse updateBatch(UUID orgId, UUID batchId, BatchRequest request) {
        Batch batch = batchRepository.findByIdAndOrganization_Id(batchId, orgId)
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
            if (instructor.getOrganizationId() == null || !instructor.getOrganizationId().equals(orgId)) {
                throw new BadRequestException("Instructor does not belong to this organization", "INSTRUCTOR_OUTSIDE_ORG");
            }
            batch.setInstructor(instructor);
        }

        Batch saved = batchRepository.save(batch);
        long count = batchStudentRepository.countByBatch_IdAndActiveTrue(batchId);
        return batchMapper.toDto(saved, count, List.of());
    }

    @Override
    @Transactional
    public void deleteBatch(UUID orgId, UUID batchId) {
        Batch batch = batchRepository.findByIdAndOrganization_Id(batchId, orgId)
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
    public List<StudentResponse> getStudentsInBatch(UUID orgId, UUID batchId) {
        Batch batch = batchRepository.findByIdAndOrganization_Id(batchId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Batch", "id", batchId));

        List<BatchStudent> batchStudents = batchStudentRepository.findActiveByBatchId(batchId);
        Map<UUID, List<CourseDto>> coursesByStudent = enrollmentRepository.findByUserIdsWithCourse(
                        batchStudents.stream().map(bs -> bs.getStudent().getId()).toList())
                .stream()
                .collect(Collectors.groupingBy(
                        e -> e.getUser().getId(),
                        Collectors.mapping(
                                e -> courseMapper.toDto(e.getCourse(), 0L),
                                Collectors.toList()
                        )
                ));

        return batchStudents.stream()
                .map(bs -> {
                    User student = bs.getStudent();
                    List<CourseDto> courses = coursesByStudent.getOrDefault(student.getId(), List.of());
                    return studentMapper.toDto(student, batchId.toString(), batch.getName(), courses);
                })
                .collect(Collectors.toList());
    }
}
