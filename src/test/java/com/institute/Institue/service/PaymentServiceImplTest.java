package com.institute.Institue.service;

import com.institute.Institue.dto.PaymentInitiateRequest;
import com.institute.Institue.dto.PaymentOrderResponse;
import com.institute.Institue.exception.BadRequestException;
import com.institute.Institue.exception.ResourceNotFoundException;
import com.institute.Institue.model.*;
import com.institute.Institue.model.enums.PaymentProvider;
import com.institute.Institue.model.enums.PaymentStatus;
import com.institute.Institue.payment.*;
import com.institute.Institue.repository.*;
import com.institute.Institue.service.impl.PaymentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentService Tests")
class PaymentServiceImplTest {

    @Mock
    private PaymentGatewayFactory gatewayFactory;
    @Mock
    private PaymentOrderRepository paymentOrderRepository;
    @Mock
    private CourseRepository courseRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private EnrollmentRepository enrollmentRepository;
    @Mock
    private PaymentGateway mockGateway;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private User student;
    private Course course;
    private Organization org;

    @BeforeEach
    void setUp() {
        org = Organization.builder().id(UUID.randomUUID()).name("Academy").build();
        student = User.builder()
                .id(UUID.randomUUID())
                .email("student@test.com")
                .firstName("Alice")
                .password("encoded")
                .role(Role.builder().id(UUID.randomUUID()).role(com.institute.Institue.model.enums.UserRole.STUDENT).build())
                .organization(org)
                .build();
        course = Course.builder()
                .id(UUID.randomUUID())
                .title("Spring Boot Masterclass")
                .price(new BigDecimal("999.00"))
                .published(true)
                .organization(org)
                .build();
    }

    @Nested
    @DisplayName("Initiate Payment")
    class InitiatePayment {

        @Test
        @DisplayName("should initiate payment successfully")
        void initiate_success() {
            PaymentInitiateRequest request = PaymentInitiateRequest.builder()
                    .courseId(course.getId().toString())
                    .provider("RAZORPAY")
                    .build();

            PaymentGatewayOrder gatewayOrder = PaymentGatewayOrder.builder()
                    .providerOrderId("order_123")
                    .paymentLink("https://rzp.io/order_123")
                    .amount(course.getPrice())
                    .currency("INR")
                    .status("created")
                    .build();

            when(userRepository.findById(student.getId())).thenReturn(Optional.of(student));
            when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));
            when(enrollmentRepository.existsByUser_IdAndCourse_Id(student.getId(), course.getId()))
                    .thenReturn(false);
            when(paymentOrderRepository.existsByStudent_IdAndCourse_IdAndStatus(
                    student.getId(), course.getId(), PaymentStatus.CREATED)).thenReturn(false);
            when(gatewayFactory.getGateway("RAZORPAY")).thenReturn(mockGateway);
            when(mockGateway.createOrder(any(PaymentGatewayRequest.class))).thenReturn(gatewayOrder);
            when(paymentOrderRepository.save(any(PaymentOrder.class))).thenAnswer(inv -> {
                PaymentOrder o = inv.getArgument(0);
                o.setId(UUID.randomUUID());
                o.setCreatedAt(Instant.now());
                return o;
            });

            PaymentOrderResponse resp = paymentService.initiatePayment(student.getId(), request);

            assertNotNull(resp);
            assertEquals("order_123", resp.getProviderOrderId());
            assertEquals("RAZORPAY", resp.getProvider());
            assertEquals(new BigDecimal("999.00"), resp.getAmount());
            assertEquals("CREATED", resp.getStatus());
            verify(mockGateway).createOrder(any());
        }

        @Test
        @DisplayName("should reject if already enrolled")
        void initiate_alreadyEnrolled() {
            PaymentInitiateRequest request = PaymentInitiateRequest.builder()
                    .courseId(course.getId().toString())
                    .provider("RAZORPAY")
                    .build();

            when(userRepository.findById(student.getId())).thenReturn(Optional.of(student));
            when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));
            when(enrollmentRepository.existsByUser_IdAndCourse_Id(student.getId(), course.getId()))
                    .thenReturn(true);

            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> paymentService.initiatePayment(student.getId(), request));
            assertTrue(ex.getMessage().contains("already enrolled"));
        }

        @Test
        @DisplayName("should reject unpublished course")
        void initiate_unpublishedCourse() {
            course.setPublished(false);
            PaymentInitiateRequest request = PaymentInitiateRequest.builder()
                    .courseId(course.getId().toString())
                    .provider("RAZORPAY")
                    .build();

            when(userRepository.findById(student.getId())).thenReturn(Optional.of(student));
            when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));

            assertThrows(BadRequestException.class, () -> paymentService.initiatePayment(student.getId(), request));
        }
    }

    @Nested
    @DisplayName("Verify Payment")
    class VerifyPayment {

        @Test
        @DisplayName("should verify and auto-enroll on success")
        void verify_successAutoEnroll() {
            UUID orderId = UUID.randomUUID();
            PaymentOrder order = PaymentOrder.builder()
                    .id(orderId)
                    .student(student)
                    .course(course)
                    .organization(org)
                    .amount(course.getPrice())
                    .currency("INR")
                    .provider(PaymentProvider.RAZORPAY)
                    .providerOrderId("order_abc")
                    .status(PaymentStatus.CREATED)
                    .createdAt(Instant.now())
                    .build();

            PaymentGatewayStatus status = PaymentGatewayStatus.builder()
                    .providerOrderId("order_abc")
                    .providerPaymentId("pay_xyz")
                    .status("captured")
                    .build();

            when(paymentOrderRepository.findById(orderId)).thenReturn(Optional.of(order));
            when(gatewayFactory.getGateway("RAZORPAY")).thenReturn(mockGateway);
            when(mockGateway.verifyPayment("order_abc")).thenReturn(status);
            when(enrollmentRepository.existsByUser_IdAndCourse_Id(student.getId(), course.getId()))
                    .thenReturn(false);
            when(enrollmentRepository.save(any(Enrollment.class))).thenAnswer(inv -> inv.getArgument(0));
            when(paymentOrderRepository.save(any(PaymentOrder.class))).thenReturn(order);

            PaymentOrderResponse resp = paymentService.verifyPayment(orderId);

            assertEquals("CAPTURED", resp.getStatus());
            verify(enrollmentRepository).save(any(Enrollment.class)); // Auto-enrolled
        }

        @Test
        @DisplayName("should mark payment as failed")
        void verify_failed() {
            UUID orderId = UUID.randomUUID();
            PaymentOrder order = PaymentOrder.builder()
                    .id(orderId).student(student).course(course).organization(org)
                    .amount(course.getPrice()).currency("INR")
                    .provider(PaymentProvider.STRIPE).providerOrderId("cs_abc")
                    .status(PaymentStatus.CREATED).createdAt(Instant.now()).build();

            PaymentGatewayStatus status = PaymentGatewayStatus.builder()
                    .providerOrderId("cs_abc").status("failed")
                    .failureReason("Card declined").build();

            when(paymentOrderRepository.findById(orderId)).thenReturn(Optional.of(order));
            when(gatewayFactory.getGateway("STRIPE")).thenReturn(mockGateway);
            when(mockGateway.verifyPayment("cs_abc")).thenReturn(status);
            when(paymentOrderRepository.save(any(PaymentOrder.class))).thenReturn(order);

            PaymentOrderResponse resp = paymentService.verifyPayment(orderId);

            assertEquals("FAILED", resp.getStatus());
            verify(enrollmentRepository, never()).save(any()); // No enrollment on failure
        }

        @Test
        @DisplayName("should throw when order not found")
        void verify_notFound() {
            UUID fakeId = UUID.randomUUID();
            when(paymentOrderRepository.findById(fakeId)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> paymentService.verifyPayment(fakeId));
        }
    }
}
