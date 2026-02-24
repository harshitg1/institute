package com.institute.Institue.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchRequest {

    @NotBlank(message = "Batch name is required")
    private String name;

    private String instructorId;
    private String duration;
    private String startTime;
    private String endTime;
}
