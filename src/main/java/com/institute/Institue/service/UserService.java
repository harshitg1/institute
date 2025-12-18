package com.institute.Institue.service;

import com.institute.Institue.dto.CreateUserRequest;
import com.institute.Institue.dto.UserResponse;

import java.util.List;

public interface UserService {
    UserResponse createUser(String orgId, CreateUserRequest req);
    List<UserResponse> listUsers(String orgId);
}

