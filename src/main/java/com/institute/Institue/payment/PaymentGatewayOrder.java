package com.institute.Institue.payment;

import lombok.*;

import java.math.BigDecimal;

/**
 * Provider-agnostic order result returned after creating an order.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentGatewayOrder {
    private String providerOrderId;
    private String paymentLink;
    private BigDecimal amount;
    private String currency;
    private String status;
}
