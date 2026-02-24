package com.institute.Institue.payment;

import lombok.*;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Provider-agnostic payment request.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentGatewayRequest {
    private BigDecimal amount;
    private String currency;
    private String description;
    private String receiptId;
    private Map<String, String> metadata;
}
