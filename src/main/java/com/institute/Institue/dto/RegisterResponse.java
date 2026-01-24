package com.institute.Institue.dto;

import lombok.Builder;
import lombok.Data;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
public class RegisterResponse {
    private String organizationId;
    private String adminUserId;
    private String accessToken;
    private String refreshToken;
    private String roles;
}
