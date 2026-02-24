package com.institute.Institue.dto;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchTransferResponse {
    private String studentId;
    private String studentName;
    private String previousBatchId;
    private String previousBatchName;
    private String newBatchId;
    private String newBatchName;
    private String reason;
    private Instant transferredAt;
}
