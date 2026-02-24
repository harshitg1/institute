package com.institute.Institue.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentStatusRequest {

    @NotBlank(message = "Status is required (ACTIVE, INACTIVE, SUSPENDED, GRADUATED)")
    private String status;
}
