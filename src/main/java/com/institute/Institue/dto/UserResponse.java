package com.institute.Institue.dto;

import com.institute.Institue.model.Organization;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UserResponse {
    private String id;
    private String email;
    private Organization organization;
    private String role; // return roles as JSON array
}
