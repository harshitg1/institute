package com.institute.Institue.service;

import com.institute.Institue.dto.*;
import com.institute.Institue.exception.BadRequestException;
import com.institute.Institue.exception.DuplicateResourceException;
import com.institute.Institue.exception.ResourceNotFoundException;
import com.institute.Institue.model.*;
import com.institute.Institue.model.enums.StudentStatus;
import com.institute.Institue.repository.*;
import com.institute.Institue.service.impl.StudentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StudentService Tests")
class StudentServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private OrganizationRepository organizationRepository;
    @Mock
    private BatchRepository batchRepository;
    @Mock
    private BatchStudentRepository batchStudentRepository;
    @Mock
    private BatchTransferLogRepository transferLogRepository;
    @Mock
    private EnrollmentRepository enrollmentRepository;
    @Mock
    private CourseRepository courseRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private StudentServiceImpl studentService;

    private UUID orgId;
    private Organization org;
    private Role studentRole;
    private Batch batch;

    @BeforeEach
    void setUp() {
        orgId = UUID.randomUUID();
        org = Organization.builder().id(orgId).name("Academy").build();
        studentRole = Role.builder().id(UUID.randomUUID()).role(com.institute.Institue.model.enums.UserRole.STUDENT).build();
        batch = Batch.builder()
                .id(UUID.randomUUID())
                .name("Morning Batch")
                .organization(org)
                .active(true)
                .build();
    }

    @Nested
    @DisplayName("Create Student")
    class CreateStudent {

        @Test
        @DisplayName("should create student with batch assignment")
        void createStudent_success() {
            CreateStudentRequest request = CreateStudentRequest.builder()
                    .email("new@student.com")
                    .firstName("Alice")
                    .lastName("Wonder")
                    .batchId(batch.getId().toString())
                    .build();

            when(organizationRepository.findById(orgId)).thenReturn(Optional.of(org));
            when(userRepository.existsByEmail("new@student.com")).thenReturn(false);
            when(batchRepository.findById(batch.getId())).thenReturn(Optional.of(batch));
            when(roleRepository.findByRole(com.institute.Institue.model.enums.UserRole.STUDENT)).thenReturn(Optional.of(studentRole));
            when(passwordEncoder.encode(any())).thenReturn("encoded");
            when(userRepository.save(any(User.class))).thenAnswer(inv -> {
                User u = inv.getArgument(0);
                u.setId(UUID.randomUUID());
                u.setCreatedAt(Instant.now());
                return u;
            });
            when(batchStudentRepository.save(any(BatchStudent.class))).thenAnswer(inv -> inv.getArgument(0));

            StudentResponse response = studentService.createStudent(orgId, request);

            assertNotNull(response);
            assertEquals("new@student.com", response.getEmail());
            assertEquals("Alice", response.getFirstName());
            assertEquals("ACTIVE", response.getStatus());
            verify(batchStudentRepository).save(any(BatchStudent.class));
        }

        @Test
        @DisplayName("should throw on duplicate email")
        void createStudent_duplicateEmail() {
            CreateStudentRequest request = CreateStudentRequest.builder()
                    .email("existing@student.com")
                    .batchId(batch.getId().toString())
                    .build();

            when(organizationRepository.findById(orgId)).thenReturn(Optional.of(org));
            when(userRepository.existsByEmail("existing@student.com")).thenReturn(true);

            assertThrows(DuplicateResourceException.class, () -> studentService.createStudent(orgId, request));
        }

        @Test
        @DisplayName("should throw when batch not found")
        void createStudent_batchNotFound() {
            UUID fakeBatchId = UUID.randomUUID();
            CreateStudentRequest request = CreateStudentRequest.builder()
                    .email("new@student.com")
                    .batchId(fakeBatchId.toString())
                    .build();

            when(organizationRepository.findById(orgId)).thenReturn(Optional.of(org));
            when(userRepository.existsByEmail(any())).thenReturn(false);
            when(batchRepository.findById(fakeBatchId)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> studentService.createStudent(orgId, request));
        }
    }

    @Nested
    @DisplayName("Update Student Status")
    class UpdateStatus {

        @Test
        @DisplayName("should update student status to SUSPENDED")
        void updateStatus_success() {
            UUID studentId = UUID.randomUUID();
            User student = User.builder()
                    .id(studentId)
                    .email("student@test.com")
                    .password("encoded")
                    .role(studentRole)
                    .studentStatus(StudentStatus.ACTIVE)
                    .organization(org)
                    .build();

            when(userRepository.findById(studentId)).thenReturn(Optional.of(student));
            when(userRepository.save(any(User.class))).thenReturn(student);
            // For getStudent() call inside updateStudentStatus
            when(batchStudentRepository.findActiveByStudentId(studentId)).thenReturn(Optional.empty());
            when(enrollmentRepository.findByUserIdWithCourse(studentId)).thenReturn(List.of());

            StudentResponse resp = studentService.updateStudentStatus(studentId, "SUSPENDED");

            assertEquals("SUSPENDED", resp.getStatus());
            assertFalse(student.isEnabled()); // SUSPENDED should disable
        }

        @Test
        @DisplayName("should throw on invalid status")
        void updateStatus_invalidStatus() {
            UUID studentId = UUID.randomUUID();
            User student = User.builder()
                    .id(studentId)
                    .email("student@test.com")
                    .password("encoded")
                    .role(studentRole)
                    .build();

            when(userRepository.findById(studentId)).thenReturn(Optional.of(student));

            assertThrows(BadRequestException.class, () -> studentService.updateStudentStatus(studentId, "INVALID"));
        }
    }

    @Nested
    @DisplayName("Batch Transfer")
    class TransferBatch {

        @Test
        @DisplayName("should transfer student to new batch")
        void transferBatch_success() {
            UUID studentId = UUID.randomUUID();
            UUID targetBatchId = UUID.randomUUID();
            User student = User.builder()
                    .id(studentId)
                    .email("student@test.com")
                    .firstName("Bob")
                    .password("encoded")
                    .role(studentRole)
                    .build();

            Batch targetBatch = Batch.builder()
                    .id(targetBatchId)
                    .name("Evening Batch")
                    .build();

            BatchStudent currentBs = BatchStudent.builder()
                    .id(UUID.randomUUID())
                    .batch(batch) // "Morning Batch"
                    .student(student)
                    .active(true)
                    .build();

            BatchTransferRequest request = BatchTransferRequest.builder()
                    .targetBatchId(targetBatchId.toString())
                    .reason("Schedule change")
                    .build();

            when(userRepository.findById(studentId)).thenReturn(Optional.of(student));
            when(batchRepository.findById(targetBatchId)).thenReturn(Optional.of(targetBatch));
            when(batchStudentRepository.findActiveByStudentId(studentId)).thenReturn(Optional.of(currentBs));
            when(batchStudentRepository.save(any(BatchStudent.class))).thenAnswer(inv -> inv.getArgument(0));
            when(transferLogRepository.save(any(BatchTransferLog.class))).thenAnswer(inv -> {
                BatchTransferLog log = inv.getArgument(0);
                log.setTransferredAt(Instant.now());
                return log;
            });

            BatchTransferResponse resp = studentService.transferBatch(studentId, request, null);

            assertNotNull(resp);
            assertEquals("Morning Batch", resp.getPreviousBatchName());
            assertEquals("Evening Batch", resp.getNewBatchName());
            assertEquals("Schedule change", resp.getReason());
            assertFalse(currentBs.isActive()); // Old membership deactivated
            verify(transferLogRepository).save(any(BatchTransferLog.class));
        }

        @Test
        @DisplayName("should throw when transferring to same batch")
        void transferBatch_sameBatch() {
            UUID studentId = UUID.randomUUID();
            User student = User.builder()
                    .id(studentId).email("s@test.com").password("e").role(studentRole).build();

            BatchStudent currentBs = BatchStudent.builder()
                    .batch(batch).student(student).active(true).build();

            BatchTransferRequest request = BatchTransferRequest.builder()
                    .targetBatchId(batch.getId().toString()).build();

            when(userRepository.findById(studentId)).thenReturn(Optional.of(student));
            when(batchRepository.findById(batch.getId())).thenReturn(Optional.of(batch));
            when(batchStudentRepository.findActiveByStudentId(studentId)).thenReturn(Optional.of(currentBs));

            assertThrows(BadRequestException.class, () -> studentService.transferBatch(studentId, request, null));
        }
    }

    @Nested
    @DisplayName("Deactivate Student")
    class DeactivateStudent {

        @Test
        @DisplayName("should deactivate student and disable account")
        void deactivate_success() {
            UUID studentId = UUID.randomUUID();
            User student = User.builder()
                    .id(studentId).email("s@test.com").password("e")
                    .role(studentRole).enabled(true).studentStatus(StudentStatus.ACTIVE).build();

            when(userRepository.findById(studentId)).thenReturn(Optional.of(student));
            when(userRepository.save(any(User.class))).thenReturn(student);

            studentService.deactivateStudent(studentId);

            assertEquals(StudentStatus.INACTIVE, student.getStudentStatus());
            assertFalse(student.isEnabled());
        }
    }
}
