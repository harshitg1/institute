package com.institute.Institue.service.impl;

import com.institute.Institue.dto.*;
import com.institute.Institue.exception.BadRequestException;
import com.institute.Institue.exception.PaymentException;
import com.institute.Institue.exception.ResourceNotFoundException;
import com.institute.Institue.mapper.PaymentOrderMapper;
import com.institute.Institue.model.*;
import com.institute.Institue.model.enums.PaymentProvider;
import com.institute.Institue.model.enums.PaymentStatus;
import com.institute.Institue.payment.*;
import com.institute.Institue.repository.*;
import com.institute.Institue.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentGatewayFactory gatewayFactory;
    private final PaymentOrderRepository paymentOrderRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final PaymentOrderMapper paymentOrderMapper;

    @Override
    @Transactional
    public PaymentOrderResponse initiatePayment(UUID studentId, PaymentInitiateRequest request) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", studentId));

        // Only users with STUDENT role can initiate payments. Admins (ORG_ADMIN / SUPER_ADMIN) manage courses and should not create payment orders.
        if (student.getRole() != null && !"STUDENT".equalsIgnoreCase(student.getRole().getRole().name())) {
            throw new BadRequestException("Only students can initiate payments", "NOT_A_STUDENT");
        }

        UUID courseId = UUID.fromString(request.getCourseId());
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", request.getCourseId()));

        if (!course.isPublished()) {
            throw new BadRequestException("Course is not available for purchase", "COURSE_NOT_PUBLISHED");
        }

        // Check if already purchased
        if (enrollmentRepository.existsByUser_IdAndCourse_Id(studentId, courseId)) {
            throw new BadRequestException("You are already enrolled in this course", "ALREADY_ENROLLED");
        }

        // Check for pending payments
        if (paymentOrderRepository.existsByStudent_IdAndCourse_IdAndStatus(
                studentId, courseId, PaymentStatus.CREATED)) {
            throw new BadRequestException("A payment is already pending for this course", "PAYMENT_PENDING");
        }

        // Parse provider
        PaymentProvider provider;
        try {
            provider = PaymentProvider.valueOf(request.getProvider().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Unsupported provider: " + request.getProvider());
        }

        // Get the gateway adapter
        PaymentGateway gateway = gatewayFactory.getGateway(provider.name());

        // Create order via the adapter
        PaymentGatewayRequest gatewayRequest = PaymentGatewayRequest.builder()
                .amount(course.getPrice())
                .currency("INR")
                .description("Purchase: " + course.getTitle())
                .receiptId("receipt_" + UUID.randomUUID().toString().substring(0, 8))
                .metadata(Map.of(
                        "studentId", studentId.toString(),
                        "courseId", courseId.toString()
                ))
                .build();

        PaymentGatewayOrder gatewayOrder = gateway.createOrder(gatewayRequest);

        // Save the order
        PaymentOrder order = PaymentOrder.builder()
                .student(student)
                .course(course)
                .organization(student.getOrganization())
                .amount(course.getPrice())
                .currency("INR")
                .provider(provider)
                .providerOrderId(gatewayOrder.getProviderOrderId())
                .paymentLink(gatewayOrder.getPaymentLink())
                .status(PaymentStatus.CREATED)
                .build();

        PaymentOrder saved = paymentOrderRepository.save(order);
        log.info("Payment order created: {} for course '{}' by student '{}'",
                saved.getId(), course.getTitle(), student.getEmail());

        return paymentOrderMapper.toDto(saved);
    }

    @Override
    @Transactional
    public PaymentOrderResponse verifyPayment(UUID requesterId, UUID orderId) {
        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", requesterId));
        PaymentOrder order = paymentOrderRepository.findByIdWithAssociations(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("PaymentOrder", "id", orderId));
        validatePaymentAccess(requester, order);

        PaymentGateway gateway = gatewayFactory.getGateway(order.getProvider().name());
        PaymentGatewayStatus status = gateway.verifyPayment(order.getProviderOrderId());

        if ("captured".equalsIgnoreCase(status.getStatus())) {
            order.setStatus(PaymentStatus.CAPTURED);
            order.setProviderPaymentId(status.getProviderPaymentId());

            // Auto-enroll student
            if (!enrollmentRepository.existsByUser_IdAndCourse_Id(
                    order.getStudent().getId(), order.getCourse().getId())) {
                Enrollment enrollment = Enrollment.builder()
                        .user(order.getStudent())
                        .course(order.getCourse())
                        .organization(order.getOrganization())
                        .purchased(true)
                        .paymentOrderId(order.getId())
                        .build();
                enrollmentRepository.save(enrollment);
                log.info("Auto-enrolled student '{}' in course '{}' after payment",
                        order.getStudent().getEmail(), order.getCourse().getTitle());
            }
        } else if ("failed".equalsIgnoreCase(status.getStatus())) {
            order.setStatus(PaymentStatus.FAILED);
            order.setFailureReason(status.getFailureReason());
        }

        paymentOrderRepository.save(order);
        return paymentOrderMapper.toDto(order);
    }

    @Override
    @Transactional
    public void handleWebhook(String providerName, Map<String, String> headers, String body) {
        PaymentGateway gateway = gatewayFactory.getGateway(providerName);

        // Verify signature
        if (!gateway.verifyWebhookSignature(headers, body)) {
            throw new PaymentException("Invalid webhook signature", "INVALID_SIGNATURE");
        }

        // Parse webhook
        WebhookResult result = gateway.parseWebhook(body);

        // Find the order with associations loaded to avoid null FK updates
        Optional<PaymentOrder> optOrder = paymentOrderRepository.findByProviderOrderIdWithAssociations(result.getProviderOrderId());

        if (optOrder.isEmpty()) {
            log.warn("Webhook received for unknown order: {}", result.getProviderOrderId());
            return;
        }

        PaymentOrder order = optOrder.get();

        if ("captured".equalsIgnoreCase(result.getStatus())) {
            order.setStatus(PaymentStatus.CAPTURED);
            order.setProviderPaymentId(result.getProviderPaymentId());
            order.setProviderSignature(result.getProviderSignature());

            // Auto-enroll
            if (!enrollmentRepository.existsByUser_IdAndCourse_Id(
                    order.getStudent().getId(), order.getCourse().getId())) {
                Enrollment enrollment = Enrollment.builder()
                        .user(order.getStudent())
                        .course(order.getCourse())
                        .organization(order.getOrganization())
                        .purchased(true)
                        .paymentOrderId(order.getId())
                        .build();
                enrollmentRepository.save(enrollment);
            }
        } else if ("failed".equalsIgnoreCase(result.getStatus())) {
            order.setStatus(PaymentStatus.FAILED);
            order.setFailureReason(result.getFailureReason());
        }

        paymentOrderRepository.save(order);
        log.info("Processed webhook for order {}: status={}", order.getId(), order.getStatus());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentOrderResponse> getPaymentsByOrganization(UUID orgId) {
        return paymentOrderRepository.findByOrganizationId(orgId).stream()
                .map(paymentOrderMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentOrderResponse getPaymentById(UUID requesterId, UUID paymentId) {
        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", requesterId));
        PaymentOrder order = paymentOrderRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("PaymentOrder", "id", paymentId));
        validatePaymentAccess(requester, order);
        return paymentOrderMapper.toDto(order);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentSummaryResponse getPaymentSummary(UUID orgId) {
        List<PaymentOrder> allOrders = paymentOrderRepository.findByOrganizationId(orgId);

        BigDecimal totalRevenue = BigDecimal.ZERO;
        long successful = 0, failed = 0;
        Map<String, PaymentSummaryResponse.MonthlyRevenue> monthlyMap = new LinkedHashMap<>();
        Map<String, PaymentSummaryResponse.CourseRevenue> courseMap = new LinkedHashMap<>();

        for (PaymentOrder order : allOrders) {
            if (order.getStatus() == PaymentStatus.CAPTURED) {
                successful++;
                totalRevenue = totalRevenue.add(order.getAmount());

                // Monthly breakdown
                String month = order.getCreatedAt().toString().substring(0, 7); // "2026-02"
                monthlyMap.computeIfAbsent(month, m ->
                                PaymentSummaryResponse.MonthlyRevenue.builder()
                                        .month(m).revenue(BigDecimal.ZERO).count(0).build())
                        .setRevenue(monthlyMap.get(month).getRevenue().add(order.getAmount()));
                monthlyMap.get(month).setCount(monthlyMap.get(month).getCount() + 1);

                // Course breakdown
                String cId = order.getCourse().getId().toString();
                courseMap.computeIfAbsent(cId, c ->
                                PaymentSummaryResponse.CourseRevenue.builder()
                                        .courseId(c)
                                        .courseTitle(order.getCourse().getTitle())
                                        .revenue(BigDecimal.ZERO)
                                        .enrollments(0)
                                        .build())
                        .setRevenue(courseMap.get(cId).getRevenue().add(order.getAmount()));
                courseMap.get(cId).setEnrollments(courseMap.get(cId).getEnrollments() + 1);
            } else if (order.getStatus() == PaymentStatus.FAILED) {
                failed++;
            }
        }

        return PaymentSummaryResponse.builder()
                .totalRevenue(totalRevenue)
                .totalTransactions(allOrders.size())
                .successfulTransactions(successful)
                .failedTransactions(failed)
                .revenueByMonth(new ArrayList<>(monthlyMap.values()))
                .revenueByCourse(new ArrayList<>(courseMap.values()))
                .build();
    }

    private void validatePaymentAccess(User requester, PaymentOrder order) {
        String roleName = requester.getRole() != null ? requester.getRole().getRole().name() : "";
        if ("SUPER_ADMIN".equals(roleName)) {
            return;
        }

        boolean isAdmin = "ORG_ADMIN".equals(roleName);

        if (isAdmin) {
            UUID requesterOrgId = requester.getOrganizationId();
            UUID orderOrgId = order.getOrganization() != null ? order.getOrganization().getId() : null;
            if (requesterOrgId != null && requesterOrgId.equals(orderOrgId)) {
                return;
            }
            throw new ResourceNotFoundException("PaymentOrder", "id", order.getId());
        }

        if (order.getStudent() != null && requester.getId().equals(order.getStudent().getId())) {
            return;
        }

        throw new ResourceNotFoundException("PaymentOrder", "id", order.getId());
    }
}
