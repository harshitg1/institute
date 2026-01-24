package com.institute.Institue.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserCreateRequest {
    private String email;
    private String firstName;
    private String lastName;
    private String organizationId;
}
