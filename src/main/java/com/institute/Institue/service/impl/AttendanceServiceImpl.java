package com.institute.Institue.service.impl;

import com.institute.Institue.dto.AttendanceRequest;
import com.institute.Institue.dto.AttendanceResponse;
import com.institute.Institue.exception.BadRequestException;
import com.institute.Institue.exception.ResourceNotFoundException;
import com.institute.Institue.model.Attendance;
import com.institute.Institue.model.Batch;
import com.institute.Institue.model.User;
import com.institute.Institue.model.enums.AttendanceStatus;
import com.institute.Institue.repository.AttendanceRepository;
import com.institute.Institue.repository.BatchRepository;
import com.institute.Institue.repository.UserRepository;
import com.institute.Institue.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final BatchRepository batchRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public AttendanceResponse markAttendance(AttendanceRequest request, UUID markedByUserId) {
        UUID batchId = UUID.fromString(request.getBatchId());
        Batch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("Batch", "id", request.getBatchId()));

        LocalDate date = LocalDate.parse(request.getDate());

        User markedBy = null;
        if (markedByUserId != null) {
            markedBy = userRepository.findById(markedByUserId).orElse(null);
        }

        List<Attendance> savedRecords = new ArrayList<>();

        for (AttendanceRequest.AttendanceRecord record : request.getRecords()) {
            UUID studentId = UUID.fromString(record.getStudentId());
            User student = userRepository.findById(studentId)
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

        return buildAttendanceResponse(batch, date, savedRecords);
    }

    @Override
    @Transactional(readOnly = true)
    public AttendanceResponse getAttendanceByBatchAndDate(UUID batchId, LocalDate date) {
        Batch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("Batch", "id", batchId));

        List<Attendance> records = attendanceRepository.findByBatchIdAndDate(batchId, date);
        return buildAttendanceResponse(batch, date, records);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceResponse.AttendanceRecordResponse> getAttendanceByStudent(UUID studentId) {
        if (!userRepository.existsById(studentId)) {
            throw new ResourceNotFoundException("Student", "id", studentId);
        }

        return attendanceRepository.findByStudentId(studentId).stream()
                .map(this::toRecordResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AttendanceResponse getAttendanceByBatch(UUID batchId) {
        Batch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("Batch", "id", batchId));

        List<Attendance> records = attendanceRepository.findByBatchId(batchId);
        return buildAttendanceResponse(batch, null, records);
    }

    @Override
    @Transactional
    public void updateAttendanceRecord(UUID attendanceId, String status, String remarks) {
        Attendance attendance = attendanceRepository.findById(attendanceId)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance", "id", attendanceId));

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

    // --- Mappers ---

    private AttendanceResponse buildAttendanceResponse(Batch batch, LocalDate date, List<Attendance> records) {
        int present = 0, absent = 0, late = 0, excused = 0;
        List<AttendanceResponse.AttendanceRecordResponse> responseRecords = new ArrayList<>();

        for (Attendance a : records) {
            switch (a.getStatus()) {
                case PRESENT -> present++;
                case ABSENT -> absent++;
                case LATE -> late++;
                case EXCUSED -> excused++;
            }
            responseRecords.add(toRecordResponse(a));
        }

        return AttendanceResponse.builder()
                .batchId(batch.getId().toString())
                .batchName(batch.getName())
                .date(date)
                .totalStudents(records.size())
                .present(present)
                .absent(absent)
                .late(late)
                .excused(excused)
                .records(responseRecords)
                .build();
    }

    private AttendanceResponse.AttendanceRecordResponse toRecordResponse(Attendance a) {
        User s = a.getStudent();
        String name = ((s.getFirstName() != null ? s.getFirstName() : "") + " " +
                (s.getLastName() != null ? s.getLastName() : "")).trim();

        return AttendanceResponse.AttendanceRecordResponse.builder()
                .id(a.getId().toString())
                .studentId(s.getId().toString())
                .studentName(name.isEmpty() ? s.getEmail() : name)
                .status(a.getStatus().name())
                .remarks(a.getRemarks())
                .build();
    }
}
