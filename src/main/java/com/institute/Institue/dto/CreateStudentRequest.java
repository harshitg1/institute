package com.institute.Institue.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateStudentRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Must be a valid email")
    private String email;

    private String firstName;
    private String lastName;

    @NotNull(message = "Batch ID is required")
    private String batchId;

    /** Optional: course IDs to assign during creation */
    private List<String> courseIds;
}
