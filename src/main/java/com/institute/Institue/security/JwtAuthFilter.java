package com.institute.Institue.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

import org.springframework.web.filter.OncePerRequestFilter;
import com.institute.Institue.tenant.TenantContext;
import java.util.Map;

public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String auth = request.getHeader("Authorization");
        try {
            if (auth != null && auth.startsWith("Bearer ")) {
                String token = auth.substring(7);
                if (jwtService.validateToken(token)) {
                    Map<String, Object> claims = jwtService.parseClaims(token);
                    if (claims != null) {
                        Object org = claims.get("organization_id");
                        if (org != null) {
                            TenantContext.setCurrentOrg(String.valueOf(org));
                        }
                        // roles are available in claims if needed
                    }
                }
            } else {
                // Optionally allow X-ORG-ID header for testing/admin flows
                String orgHeader = request.getHeader("X-ORG-ID");
                if (orgHeader != null && !orgHeader.isBlank()) {
                    TenantContext.setCurrentOrg(orgHeader);
                }
            }

            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }
}
