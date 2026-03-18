package com.institute.Institue.controller;

import com.institute.Institue.tenant.TenantContext;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/debug")
public class DebugController {

    @GetMapping("/whoami")
    public ResponseEntity<Map<String,Object>> whoami() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String principal = auth == null ? null : String.valueOf(auth.getPrincipal());
        List<String> roles = auth == null ? List.of() : auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
        String orgId = TenantContext.getCurrentOrgId();

        Map<String,Object> body = new HashMap<>();
        body.put("principal", principal);
        body.put("roles", roles);
        body.put("organizationId", orgId);
        return ResponseEntity.ok(body);
    }
}
