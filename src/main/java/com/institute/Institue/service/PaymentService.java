package com.institute.Institue.service;

import com.institute.Institue.dto.*;

import java.util.List;
import java.util.UUID;

public interface PaymentService {

    PaymentOrderResponse initiatePayment(UUID studentId, PaymentInitiateRequest request);

    PaymentOrderResponse verifyPayment(UUID orderId);

    void handleWebhook(String provider, java.util.Map<String, String> headers, String body);

    List<PaymentOrderResponse> getPaymentsByOrganization(UUID orgId);

    PaymentOrderResponse getPaymentById(UUID paymentId);

    PaymentSummaryResponse getPaymentSummary(UUID orgId);
}
