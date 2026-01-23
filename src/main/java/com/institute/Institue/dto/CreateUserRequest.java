package com.institute.Institue.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateUserRequest {
    // username removed; use email as unique identifier
    private String email;
    private String roles; // comma-separated roles
}
