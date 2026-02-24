package com.institute.Institue.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchTransferRequest {

    @NotBlank(message = "Target batch ID is required")
    @Size(max = 36, message = "Target batch ID must not exceed 36 characters")
    private String targetBatchId;

    private String reason;
}
