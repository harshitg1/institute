package com.institute.Institue.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentInitiateRequest {

    @NotBlank(message = "Course ID is required")
    private String courseId;

    @NotBlank(message = "Payment provider is required (RAZORPAY, STRIPE)")
    private String provider;
}
