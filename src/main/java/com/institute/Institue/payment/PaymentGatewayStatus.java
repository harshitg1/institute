package com.institute.Institue.payment;

import lombok.*;

/**
 * Provider-agnostic payment status response.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentGatewayStatus {
    private String providerOrderId;
    private String providerPaymentId;
    private String status; // "captured", "failed", "pending"
    private String failureReason;
}
