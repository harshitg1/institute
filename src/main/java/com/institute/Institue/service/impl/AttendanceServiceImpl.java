package com.institute.Institue.service.impl;

import com.institute.Institue.dto.AttendanceRequest;
import com.institute.Institue.dto.AttendanceResponse;
import com.institute.Institue.exception.BadRequestException;
import com.institute.Institue.exception.ResourceNotFoundException;
import com.institute.Institue.mapper.AttendanceMapper;
import com.institute.Institue.model.Attendance;
import com.institute.Institue.model.Batch;
import com.institute.Institue.model.User;
import com.institute.Institue.model.enums.AttendanceStatus;
import com.institute.Institue.repository.AttendanceRepository;
import com.institute.Institue.repository.BatchRepository;
import com.institute.Institue.repository.BatchStudentRepository;
import com.institute.Institue.repository.UserRepository;
import com.institute.Institue.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final BatchRepository batchRepository;
    private final BatchStudentRepository batchStudentRepository;
    private final UserRepository userRepository;
    private final AttendanceMapper attendanceMapper;

    @Override
    @Transactional
    public AttendanceResponse markAttendance(UUID orgId, UUID batchId, AttendanceRequest request, UUID markedByUserId) {
        if (!batchId.toString().equals(request.getBatchId())) {
            throw new BadRequestException("Batch ID in path and body must match", "BATCH_ID_MISMATCH");
        }

        Batch batch = batchRepository.findByIdAndOrganization_Id(batchId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Batch", "id", request.getBatchId()));

        LocalDate date = LocalDate.parse(request.getDate());

        User markedBy = null;
        if (markedByUserId != null) {
            markedBy = userRepository.findById(markedByUserId).orElse(null);
        }

        List<Attendance> savedRecords = new ArrayList<>();

        Set<UUID> seenStudentIds = new HashSet<>();
        for (AttendanceRequest.AttendanceRecord record : request.getRecords()) {
            UUID studentId = UUID.fromString(record.getStudentId());
            if (!seenStudentIds.add(studentId)) {
                throw new BadRequestException("Duplicate student in attendance payload: " + record.getStudentId(),
                        "DUPLICATE_ATTENDANCE_STUDENT");
            }

            if (batchStudentRepository.findActiveByStudentIdAndBatchId(studentId, batchId).isEmpty()) {
                throw new BadRequestException("Student is not actively assigned to this batch", "STUDENT_NOT_IN_BATCH");
            }

            User student = userRepository.findByIdAndOrganization_Id(studentId, orgId)
                    .orElseThrow(() -> new ResourceNotFoundException("Student", "id", record.getStudentId()));

            AttendanceStatus status;
            try {
                status = AttendanceStatus.valueOf(record.getStatus().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Invalid attendance status: " + record.getStatus() +
                        ". Valid: PRESENT, ABSENT, LATE, EXCUSED");
            }

            // Check for duplicate
            if (attendanceRepository.existsByBatch_IdAndStudent_IdAndDate(batchId, studentId, date)) {
                throw new BadRequestException("Attendance already marked for student " +
                        student.getEmail() + " on " + date);
            }

            Attendance attendance = Attendance.builder()
                    .batch(batch)
                    .student(student)
                    .date(date)
                    .status(status)
                    .remarks(record.getRemarks())
                    .markedBy(markedBy)
                    .build();

            savedRecords.add(attendanceRepository.save(attendance));
        }

        log.info("Marked attendance for {} students in batch '{}' on {}",
                savedRecords.size(), batch.getName(), date);

        return attendanceMapper.toAttendanceResponse(batch, date, savedRecords);
    }

    @Override
    @Transactional(readOnly = true)
    public AttendanceResponse getAttendanceByBatchAndDate(UUID orgId, UUID batchId, LocalDate date) {
        Batch batch = batchRepository.findByIdAndOrganization_Id(batchId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Batch", "id", batchId));

        List<Attendance> records = attendanceRepository.findByBatchIdAndDate(batchId, date);
        return attendanceMapper.toAttendanceResponse(batch, date, records);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceResponse.AttendanceRecordResponse> getAttendanceByStudent(UUID orgId, UUID studentId) {
        if (userRepository.findByIdAndOrganization_Id(studentId, orgId).isEmpty()) {
            throw new ResourceNotFoundException("Student", "id", studentId);
        }

        return attendanceRepository.findByStudentId(studentId).stream()
                .map(attendanceMapper::toRecordResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AttendanceResponse getAttendanceByBatch(UUID orgId, UUID batchId) {
        Batch batch = batchRepository.findByIdAndOrganization_Id(batchId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Batch", "id", batchId));

        List<Attendance> records = attendanceRepository.findByBatchId(batchId);
        return attendanceMapper.toAttendanceResponse(batch, null, records);
    }

    @Override
    @Transactional
    public void updateAttendanceRecord(UUID orgId, UUID attendanceId, String status, String remarks) {
        Attendance attendance = attendanceRepository.findById(attendanceId)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance", "id", attendanceId));
        if (attendance.getBatch().getOrganization() == null ||
                !attendance.getBatch().getOrganization().getId().equals(orgId)) {
            throw new ResourceNotFoundException("Attendance", "id", attendanceId);
        }

        if (status != null) {
            try {
                attendance.setStatus(AttendanceStatus.valueOf(status.toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Invalid status: " + status);
            }
        }
        if (remarks != null) {
            attendance.setRemarks(remarks);
        }
        attendance.setUpdatedAt(Instant.now());
        attendanceRepository.save(attendance);
    }
}
