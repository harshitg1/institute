package com.institute.Institue.service;

import com.institute.Institue.dto.BatchRequest;
import com.institute.Institue.dto.BatchResponse;
import com.institute.Institue.dto.StudentResponse;
import com.institute.Institue.exception.BadRequestException;
import com.institute.Institue.exception.DuplicateResourceException;
import com.institute.Institue.exception.ResourceNotFoundException;
import com.institute.Institue.model.*;
import com.institute.Institue.model.enums.StudentStatus;
import com.institute.Institue.repository.*;
import com.institute.Institue.service.impl.BatchServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BatchService Tests")
class BatchServiceImplTest {

    @Mock
    private BatchRepository batchRepository;
    @Mock
    private BatchStudentRepository batchStudentRepository;
    @Mock
    private OrganizationRepository organizationRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private EnrollmentRepository enrollmentRepository;

    @InjectMocks
    private BatchServiceImpl batchService;

    private UUID orgId;
    private Organization org;
    private User instructor;

    @BeforeEach
    void setUp() {
        orgId = UUID.randomUUID();
        org = Organization.builder().id(orgId).name("Test Academy").build();
        instructor = User.builder()
                .id(UUID.randomUUID())
                .email("instructor@test.com")
                .firstName("John")
                .lastName("Doe")
                .password("encoded")
                .role(Role.builder().id(UUID.randomUUID()).name("TUTOR").build())
                .organization(org)
                .build();
    }

    @Nested
    @DisplayName("Create Batch")
    class CreateBatch {

        @Test
        @DisplayName("should create batch successfully")
        void createBatch_success() {
            BatchRequest request = BatchRequest.builder()
                    .name("Morning Batch")
                    .duration("3 months")
                    .startTime("09:00")
                    .endTime("11:00")
                    .build();

            when(organizationRepository.findById(orgId)).thenReturn(Optional.of(org));
            when(batchRepository.existsByNameAndOrganization_Id("Morning Batch", orgId)).thenReturn(false);
            when(batchRepository.save(any(Batch.class))).thenAnswer(inv -> {
                Batch b = inv.getArgument(0);
                b.setId(UUID.randomUUID());
                b.setCreatedAt(Instant.now());
                return b;
            });

            BatchResponse response = batchService.createBatch(orgId, request);

            assertNotNull(response);
            assertEquals("Morning Batch", response.getName());
            assertEquals("3 months", response.getDuration());
            assertEquals("09:00", response.getStartTime());
            verify(batchRepository).save(any(Batch.class));
        }

        @Test
        @DisplayName("should create batch with instructor")
        void createBatch_withInstructor() {
            BatchRequest request = BatchRequest.builder()
                    .name("Evening Batch")
                    .instructorId(instructor.getId().toString())
                    .build();

            when(organizationRepository.findById(orgId)).thenReturn(Optional.of(org));
            when(batchRepository.existsByNameAndOrganization_Id("Evening Batch", orgId)).thenReturn(false);
            when(userRepository.findById(instructor.getId())).thenReturn(Optional.of(instructor));
            when(batchRepository.save(any(Batch.class))).thenAnswer(inv -> {
                Batch b = inv.getArgument(0);
                b.setId(UUID.randomUUID());
                return b;
            });

            BatchResponse response = batchService.createBatch(orgId, request);

            assertNotNull(response);
            assertEquals("John Doe", response.getInstructorName());
        }

        @Test
        @DisplayName("should throw on duplicate batch name")
        void createBatch_duplicateName() {
            BatchRequest request = BatchRequest.builder().name("Existing Batch").build();

            when(organizationRepository.findById(orgId)).thenReturn(Optional.of(org));
            when(batchRepository.existsByNameAndOrganization_Id("Existing Batch", orgId)).thenReturn(true);

            assertThrows(DuplicateResourceException.class, () -> batchService.createBatch(orgId, request));
        }

        @Test
        @DisplayName("should throw when organization not found")
        void createBatch_orgNotFound() {
            BatchRequest request = BatchRequest.builder().name("Batch").build();

            when(organizationRepository.findById(orgId)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> batchService.createBatch(orgId, request));
        }
    }

    @Nested
    @DisplayName("List Batches")
    class ListBatches {

        @Test
        @DisplayName("should list batches with student counts")
        void listBatches_success() {
            UUID batchId = UUID.randomUUID();
            Batch batch = Batch.builder()
                    .id(batchId)
                    .name("Morning")
                    .organization(org)
                    .instructor(instructor)
                    .active(true)
                    .createdAt(Instant.now())
                    .build();

            when(batchRepository.findByOrganization_Id(orgId)).thenReturn(List.of(batch));
            when(batchStudentRepository.countByBatch_IdAndActiveTrue(batchId)).thenReturn(5L);

            List<BatchResponse> batches = batchService.listBatches(orgId);

            assertEquals(1, batches.size());
            assertEquals(5, batches.get(0).getStudentCount());
            assertEquals("Morning", batches.get(0).getName());
        }
    }

    @Nested
    @DisplayName("Delete Batch")
    class DeleteBatch {

        @Test
        @DisplayName("should delete empty batch")
        void deleteBatch_empty() {
            UUID batchId = UUID.randomUUID();
            Batch batch = Batch.builder().id(batchId).name("Empty Batch").build();

            when(batchRepository.findById(batchId)).thenReturn(Optional.of(batch));
            when(batchStudentRepository.countByBatch_IdAndActiveTrue(batchId)).thenReturn(0L);

            assertDoesNotThrow(() -> batchService.deleteBatch(batchId));
            verify(batchRepository).delete(batch);
        }

        @Test
        @DisplayName("should refuse to delete batch with active students")
        void deleteBatch_notEmpty() {
            UUID batchId = UUID.randomUUID();
            Batch batch = Batch.builder().id(batchId).name("Full Batch").build();

            when(batchRepository.findById(batchId)).thenReturn(Optional.of(batch));
            when(batchStudentRepository.countByBatch_IdAndActiveTrue(batchId)).thenReturn(3L);

            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> batchService.deleteBatch(batchId));
            assertTrue(ex.getMessage().contains("3 active students"));
        }
    }

    @Nested
    @DisplayName("Get Students In Batch")
    class GetStudentsInBatch {

        @Test
        @DisplayName("should return students with their courses")
        void getStudentsInBatch_success() {
            UUID batchId = UUID.randomUUID();
            User student = User.builder()
                    .id(UUID.randomUUID())
                    .email("student@test.com")
                    .firstName("Jane")
                    .lastName("Smith")
                    .password("encoded")
                    .role(Role.builder().id(UUID.randomUUID()).name("STUDENT").build())
                    .studentStatus(StudentStatus.ACTIVE)
                    .createdAt(Instant.now())
                    .build();

            BatchStudent bs = BatchStudent.builder()
                    .id(UUID.randomUUID())
                    .student(student)
                    .active(true)
                    .build();

            when(batchRepository.existsById(batchId)).thenReturn(true);
            when(batchStudentRepository.findActiveByBatchId(batchId)).thenReturn(List.of(bs));
            when(enrollmentRepository.findByUserIdWithCourse(student.getId())).thenReturn(List.of());

            List<StudentResponse> students = batchService.getStudentsInBatch(batchId);

            assertEquals(1, students.size());
            assertEquals("Jane", students.get(0).getFirstName());
            assertEquals("ACTIVE", students.get(0).getStatus());
        }
    }
}
