package com.institute.Institue.security;

import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class JwtService {

    private final byte[] secretBytes;
    private final long accessTtlMs = 1000L * 60 * 60;        // 1 Hour
    private final long refreshTtlMs = 1000L * 60 * 60 * 24 * 7; //7 days

    public JwtService() {
        String secret = System.getenv("JWT_SECRET");
        if (secret == null || secret.isBlank()) {
            secret = "dev-secret-key-change-me-please-0123456789";
        }
        this.secretBytes = secret.getBytes(StandardCharsets.UTF_8);
    }

    private String base64UrlEncode(byte[] data) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(data);
    }

    private byte[] base64UrlDecode(String s) {
        return Base64.getUrlDecoder().decode(s);
    }

    private String hmacSha256(String data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec keySpec = new SecretKeySpec(secretBytes, "HmacSHA256");
        mac.init(keySpec);
        byte[] sig = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return base64UrlEncode(sig);
    }
    // --- Core Token Generation Helper ---
    private String createToken(String subject, Map<String, Object> claims, long ttlMs) {
        try {
            long now = System.currentTimeMillis();
            long exp = now + ttlMs;

            Map<String, Object> header = new HashMap<>();
            header.put("alg", "HS256");
            header.put("typ", "JWT");

            Map<String, Object> payload = new HashMap<>(claims);
            payload.put("sub", subject);
            payload.put("iat", now / 1000); // Standard JWT iat is in seconds
            payload.put("exp", exp / 1000); // Standard JWT exp is in seconds

            String headerJson = JsonUtil.toJson(header);
            String payloadJson = JsonUtil.toJson(payload);

            String signingInput = base64UrlEncode(headerJson.getBytes(StandardCharsets.UTF_8)) + "." +
                    base64UrlEncode(payloadJson.getBytes(StandardCharsets.UTF_8));
            String signature = hmacSha256(signingInput);

            return signingInput + "." + signature;
        } catch (Exception e) {
            throw new RuntimeException("JWT generation failed", e);
        }
    }

    // --- Generate Access Token (Full Claims) ---
    public String generateAccessToken(String subject, String organizationId, String roleId, List<String> roles) {
        Map<String, Object> claims = new HashMap<>();
        if (organizationId != null) claims.put("organization_id", organizationId);
        if (roleId != null) claims.put("role_id", roleId);
        if (roles != null) claims.put("roles", roles);
        claims.put("token_type", "ACCESS");

        return createToken(subject, claims, accessTtlMs);
    }

    // --- Generate Refresh Token (Minimal Claims) ---
    public String generateRefreshToken(String subject) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("token_type", "REFRESH");
        return createToken(subject, claims, refreshTtlMs);
    }
    public boolean validateToken(String token) {
        try {
            Map<String, Object> claims = parseClaims(token);
            if (claims == null) return false;

            Object expObj = claims.get("exp");
            if (expObj instanceof Number) {
                long expSeconds = ((Number) expObj).longValue();
                // Convert seconds back to milliseconds for comparison
                return System.currentTimeMillis() < (expSeconds * 1000L);
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public Map<String, Object> parseClaims(String token) {
        try {
            if (token == null) return null;
            String[] parts = token.split("\\.");
            if (parts.length != 3) return null;
            String signingInput = parts[0] + "." + parts[1];
            String signature = parts[2];
            String expected = hmacSha256(signingInput);
            if (!MessageDigestUtil.secureEquals(expected, signature)) return null;

            byte[] payloadBytes = base64UrlDecode(parts[1]);
            String payloadJson = new String(payloadBytes, StandardCharsets.UTF_8);
            return JsonUtil.fromJsonToMap(payloadJson);
        } catch (Exception e) {
            return null;
        }
    }
}
