package com.institute.Institue.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UserResponse {
    private String id;
    private String email;
    private String organizationId;
    private List<String> roles; // return roles as JSON array
}
