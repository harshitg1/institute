package com.institute.Institue.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentOrderResponse {
    private String orderId;
    private String providerOrderId;
    private String provider;
    private BigDecimal amount;
    private String currency;
    private String courseId;
    private String courseTitle;
    private String studentId;
    private String studentName;
    private String paymentLink;
    private String status;
    private String failureReason;
    private Instant createdAt;
}
