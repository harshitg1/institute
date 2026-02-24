package com.institute.Institue.service.impl;

import com.institute.Institue.dto.CreateUserRequest;
import com.institute.Institue.dto.UserResponse;
import com.institute.Institue.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class UserServiceImpl implements UserService {


    @Override
    public UserResponse createUser(String orgId, CreateUserRequest req) {
        return null;
    }

    @Override
    public List<UserResponse> listUsers(String orgId) {
        return java.util.List.of();
    }
}
