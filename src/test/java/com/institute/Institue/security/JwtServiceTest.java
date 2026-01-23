package com.institute.Institue.security;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;

public class JwtServiceTest {

    @Test
    public void generateAndParseToken_containsClaimsAndValid() {
        JwtService svc = new JwtService();
        String token = svc.generateToken("alice@example.com", "org-uuid", List.of("SUPER_ADMIN", "TUTOR"));
        assertNotNull(token);
        assertTrue(svc.validateToken(token));

        Map<String, Object> claims = svc.parseClaims(token);
        assertNotNull(claims);
        assertEquals("alice@example.com", claims.get("sub"));
        assertEquals("org-uuid", claims.get("organization_id"));
        assertTrue(claims.get("roles") instanceof List);
    }
}
