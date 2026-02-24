package com.institute.Institue.controller;

import com.institute.Institue.dto.*;
import com.institute.Institue.model.User;
import com.institute.Institue.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    // ==================== STUDENT ENDPOINTS ====================

    /**
     * Initiate a payment order for a course purchase
     */
    @PostMapping("/payments/initiate")
    public ResponseEntity<ApiResponse<PaymentOrderResponse>> initiatePayment(
            @AuthenticationPrincipal User student,
            @Valid @RequestBody PaymentInitiateRequest request) {
        PaymentOrderResponse response = paymentService.initiatePayment(student.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Verify payment status for an order
     */
    @GetMapping("/payments/verify/{orderId}")
    public ResponseEntity<ApiResponse<PaymentOrderResponse>> verifyPayment(
            @PathVariable UUID orderId) {
        PaymentOrderResponse response = paymentService.verifyPayment(orderId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ==================== WEBHOOK (PUBLIC, SIGNATURE-VERIFIED)
    // ====================

    /**
     * Handle payment provider webhooks (Razorpay, Stripe, etc.)
     * Public endpoint — authentication is via webhook signature verification.
     */
    @PostMapping("/payments/webhook/{provider}")
    public ResponseEntity<String> handleWebhook(
            @PathVariable String provider,
            HttpServletRequest request) throws IOException {

        // Extract headers
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            headers.put(name.toLowerCase(), request.getHeader(name));
        }

        // Read body
        String body;
        try (BufferedReader reader = request.getReader()) {
            body = reader.lines().collect(Collectors.joining("\n"));
        }

        paymentService.handleWebhook(provider.toUpperCase(), headers, body);
        return ResponseEntity.ok("OK");
    }

    // ==================== ADMIN ENDPOINTS ====================

    /**
     * View all payment transactions for the organization
     */
    @GetMapping("/admin/payments")
    public ResponseEntity<ApiResponse<List<PaymentOrderResponse>>> getPayments(
            @AuthenticationPrincipal User admin) {
        UUID orgId = admin.getOrganizationId();
        List<PaymentOrderResponse> payments = paymentService.getPaymentsByOrganization(orgId);
        return ResponseEntity.ok(ApiResponse.success(payments));
    }

    /**
     * View a specific payment transaction
     */
    @GetMapping("/admin/payments/{id}")
    public ResponseEntity<ApiResponse<PaymentOrderResponse>> getPaymentById(@PathVariable UUID id) {
        PaymentOrderResponse response = paymentService.getPaymentById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Revenue summary & analytics
     */
    @GetMapping("/admin/payments/summary")
    public ResponseEntity<ApiResponse<PaymentSummaryResponse>> getPaymentSummary(
            @AuthenticationPrincipal User admin) {
        UUID orgId = admin.getOrganizationId();
        PaymentSummaryResponse response = paymentService.getPaymentSummary(orgId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
