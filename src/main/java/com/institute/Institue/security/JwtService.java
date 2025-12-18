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
    private final long ttlMs = 1000L * 60 * 60 * 24; // 24 hours

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

    public String generateToken(String subject, String organizationId, List<String> roles) {
        try {
            long now = System.currentTimeMillis();
            long exp = now + ttlMs;

            Map<String, Object> header = new HashMap<>();
            header.put("alg", "HS256");
            header.put("typ", "JWT");

            Map<String, Object> payload = new HashMap<>();
            payload.put("sub", subject);
            if (organizationId != null) payload.put("organization_id", organizationId);
            if (roles != null) payload.put("roles", roles.stream().collect(Collectors.toList()));
            payload.put("iat", now);
            payload.put("exp", exp);

            String headerJson = JsonUtil.toJson(header);
            String payloadJson = JsonUtil.toJson(payload);

            String signingInput = base64UrlEncode(headerJson.getBytes(StandardCharsets.UTF_8)) + "." + base64UrlEncode(payloadJson.getBytes(StandardCharsets.UTF_8));
            String signature = hmacSha256(signingInput);

            return signingInput + "." + signature;
        } catch (Exception e) {
            return null;
        }
    }

    public boolean validateToken(String token) {
        try {
            Map<String, Object> claims = parseClaims(token);
            if (claims == null) return false;
            Object expObj = claims.get("exp");
            if (expObj instanceof Number) {
                long exp = ((Number) expObj).longValue();
                return System.currentTimeMillis() < exp;
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
