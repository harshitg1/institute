package com.institute.Institue.controller;

import com.institute.Institue.dto.CreateUserRequest;
import com.institute.Institue.dto.UserResponse;
import com.institute.Institue.service.UserService;
import com.institute.Institue.tenant.TenantContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/org")
public class OrganizationController {

    private final UserService userService;

    public OrganizationController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/users")
    public ResponseEntity<UserResponse> createUser(@RequestBody CreateUserRequest req) {
        String orgId = TenantContext.getCurrentOrgId();
        if (orgId == null) return ResponseEntity.status(403).build();
        UserResponse created = userService.createUser(orgId, req);
        if (created == null) return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(created);
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> listUsers() {
        String orgId = TenantContext.getCurrentOrgId();
        if (orgId == null) return ResponseEntity.status(403).build();
        return ResponseEntity.ok(userService.listUsers(orgId));
    }
}

