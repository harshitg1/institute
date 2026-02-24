package com.institute.Institue.security;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;

public class JwtServiceTest {

    @Test
    public void generateAndParseToken_containsClaimsAndValid() {
        JwtService svc = new JwtService();
        String token = svc.generateAccessToken("alice@example.com", "org-uuid", "role-uuid", List.of("SUPER_ADMIN", "TUTOR"));
        assertNotNull(token);
        assertTrue(svc.validateToken(token));

        Map<String, Object> claims = svc.parseClaims(token);
        assertNotNull(claims);
        assertEquals("alice@example.com", claims.get("sub"));
        assertEquals("org-uuid", claims.get("organization_id"));
        assertEquals("role-uuid", claims.get("role_id"));
        assertTrue(claims.get("roles") instanceof List);
    }

    @Test
    public void generateRefreshToken_containsMinimalClaims() {
        JwtService svc = new JwtService();
        String token = svc.generateRefreshToken("bob@example.com");
        assertNotNull(token);
        assertTrue(svc.validateToken(token));

        Map<String, Object> claims = svc.parseClaims(token);
        assertNotNull(claims);
        assertEquals("bob@example.com", claims.get("sub"));
        assertEquals("REFRESH", claims.get("token_type"));
        assertNull(claims.get("roles")); // Refresh token has no roles
    }

    @Test
    public void validateToken_invalidToken_returnsFalse() {
        JwtService svc = new JwtService();
        assertFalse(svc.validateToken("this.is.invalid"));
        assertFalse(svc.validateToken(null));
        assertFalse(svc.validateToken(""));
    }
}
