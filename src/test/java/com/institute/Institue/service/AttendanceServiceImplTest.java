package com.institute.Institue.service;

import com.institute.Institue.dto.AttendanceRequest;
import com.institute.Institue.dto.AttendanceResponse;
import com.institute.Institue.exception.BadRequestException;
import com.institute.Institue.exception.ResourceNotFoundException;
import com.institute.Institue.model.*;
import com.institute.Institue.model.enums.AttendanceStatus;
import com.institute.Institue.repository.AttendanceRepository;
import com.institute.Institue.repository.BatchRepository;
import com.institute.Institue.repository.UserRepository;
import com.institute.Institue.service.impl.AttendanceServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AttendanceService Tests")
class AttendanceServiceImplTest {

    @Mock
    private AttendanceRepository attendanceRepository;
    @Mock
    private BatchRepository batchRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AttendanceServiceImpl attendanceService;

    private Batch batch;
    private User student1;
    private User student2;

    @BeforeEach
    void setUp() {
        batch = Batch.builder()
                .id(UUID.randomUUID())
                .name("Morning Batch")
                .build();

        student1 = User.builder()
                .id(UUID.randomUUID())
                .email("s1@test.com")
                .firstName("Alice")
                .lastName("A")
                .password("encoded")
                .role(Role.builder().id(UUID.randomUUID()).role(com.institute.Institue.model.enums.UserRole.STUDENT).build())
                .build();

        student2 = User.builder()
                .id(UUID.randomUUID())
                .email("s2@test.com")
                .firstName("Bob")
                .lastName("B")
                .password("encoded")
                .role(Role.builder().id(UUID.randomUUID()).role(com.institute.Institue.model.enums.UserRole.STUDENT).build())
                .build();
    }

    @Nested
    @DisplayName("Mark Attendance")
    class MarkAttendance {

        @Test
        @DisplayName("should mark bulk attendance successfully")
        void markAttendance_success() {
            AttendanceRequest request = AttendanceRequest.builder()
                    .batchId(batch.getId().toString())
                    .date("2026-02-10")
                    .records(List.of(
                            AttendanceRequest.AttendanceRecord.builder()
                                    .studentId(student1.getId().toString())
                                    .status("PRESENT")
                                    .build(),
                            AttendanceRequest.AttendanceRecord.builder()
                                    .studentId(student2.getId().toString())
                                    .status("ABSENT")
                                    .remarks("Sick leave")
                                    .build()))
                    .build();

            when(batchRepository.findById(batch.getId())).thenReturn(Optional.of(batch));
            when(userRepository.findById(student1.getId())).thenReturn(Optional.of(student1));
            when(userRepository.findById(student2.getId())).thenReturn(Optional.of(student2));
            when(attendanceRepository.existsByBatch_IdAndStudent_IdAndDate(any(), any(), any())).thenReturn(false);
            when(attendanceRepository.save(any(Attendance.class))).thenAnswer(inv -> {
                Attendance a = inv.getArgument(0);
                a.setId(UUID.randomUUID());
                return a;
            });

            AttendanceResponse response = attendanceService.markAttendance(request, null);

            assertNotNull(response);
            assertEquals(batch.getName(), response.getBatchName());
            assertEquals(2, response.getTotalStudents());
            assertEquals(1, response.getPresent());
            assertEquals(1, response.getAbsent());
            assertEquals(0, response.getLate());
            verify(attendanceRepository, times(2)).save(any(Attendance.class));
        }

        @Test
        @DisplayName("should reject duplicate attendance")
        void markAttendance_duplicateThrows() {
            AttendanceRequest request = AttendanceRequest.builder()
                    .batchId(batch.getId().toString())
                    .date("2026-02-10")
                    .records(List.of(
                            AttendanceRequest.AttendanceRecord.builder()
                                    .studentId(student1.getId().toString())
                                    .status("PRESENT")
                                    .build()))
                    .build();

            when(batchRepository.findById(batch.getId())).thenReturn(Optional.of(batch));
            when(userRepository.findById(student1.getId())).thenReturn(Optional.of(student1));
            when(attendanceRepository.existsByBatch_IdAndStudent_IdAndDate(
                    batch.getId(), student1.getId(), LocalDate.parse("2026-02-10")))
                    .thenReturn(true);

            assertThrows(BadRequestException.class, () -> attendanceService.markAttendance(request, null));
        }

        @Test
        @DisplayName("should reject invalid attendance status")
        void markAttendance_invalidStatus() {
            AttendanceRequest request = AttendanceRequest.builder()
                    .batchId(batch.getId().toString())
                    .date("2026-02-10")
                    .records(List.of(
                            AttendanceRequest.AttendanceRecord.builder()
                                    .studentId(student1.getId().toString())
                                    .status("INVALID_STATUS")
                                    .build()))
                    .build();

            when(batchRepository.findById(batch.getId())).thenReturn(Optional.of(batch));
            when(userRepository.findById(student1.getId())).thenReturn(Optional.of(student1));

            assertThrows(BadRequestException.class, () -> attendanceService.markAttendance(request, null));
        }
    }

    @Nested
    @DisplayName("Query Attendance")
    class QueryAttendance {

        @Test
        @DisplayName("should get attendance by batch and date")
        void getByBatchAndDate() {
            LocalDate date = LocalDate.parse("2026-02-10");
            Attendance a1 = Attendance.builder()
                    .id(UUID.randomUUID()).batch(batch).student(student1)
                    .date(date).status(AttendanceStatus.PRESENT).build();
            Attendance a2 = Attendance.builder()
                    .id(UUID.randomUUID()).batch(batch).student(student2)
                    .date(date).status(AttendanceStatus.LATE).remarks("Traffic").build();

            when(batchRepository.findById(batch.getId())).thenReturn(Optional.of(batch));
            when(attendanceRepository.findByBatchIdAndDate(batch.getId(), date))
                    .thenReturn(List.of(a1, a2));

            AttendanceResponse response = attendanceService
                    .getAttendanceByBatchAndDate(batch.getId(), date);

            assertEquals(2, response.getTotalStudents());
            assertEquals(1, response.getPresent());
            assertEquals(1, response.getLate());
            assertEquals("Alice A", response.getRecords().get(0).getStudentName());
        }

        @Test
        @DisplayName("should throw when batch not found")
        void getByBatchAndDate_notFound() {
            UUID fakeBatchId = UUID.randomUUID();
            when(batchRepository.findById(fakeBatchId)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> attendanceService.getAttendanceByBatchAndDate(fakeBatchId, LocalDate.now()));
        }
    }
}
