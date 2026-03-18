package com.institute.Institue.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationCreateRequest {
    @NotBlank(message = "Organization name is required")
    private String name;

    @NotBlank(message = "Admin email is required")
    @Email(message = "Valid admin email is required")
    private String adminEmail;

    @NotBlank(message = "Admin password is required")
    private String adminPassword;

    @NotBlank(message = "Admin first name is required")
    private String adminFirstName;

    @NotBlank(message = "Admin last name is required")
    private String adminLastName;
}
