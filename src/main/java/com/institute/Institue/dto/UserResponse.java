package com.institute.Institue.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserResponse {
    private String id;
    private String username;
    private String email;
    private String organizationId;
    private String roles;
}

