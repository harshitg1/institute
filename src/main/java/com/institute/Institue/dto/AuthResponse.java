package com.institute.Institue.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AuthResponse {
    private String token;
    private String organizationId;
    private String roles;

    public AuthResponse() {}

    public AuthResponse(String token) { this.token = token; }

    public AuthResponse(String token, String organizationId, String roles) {
        this.token = token;
        this.organizationId = organizationId;
        this.roles = roles;
    }

}
