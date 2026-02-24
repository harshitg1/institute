package com.institute.Institue.payment;

import lombok.*;

/**
 * Parsed result from a provider webhook callback.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebhookResult {
    private String event; // e.g. "payment.captured", "payment.failed"
    private String providerOrderId;
    private String providerPaymentId;
    private String providerSignature;
    private String status; // normalized: "captured", "failed"
    private String failureReason;
}
