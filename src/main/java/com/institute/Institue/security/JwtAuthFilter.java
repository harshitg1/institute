package com.institute.Institue.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.lang.NonNull;
import com.institute.Institue.tenant.TenantContext;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.institute.Institue.security.CustomUserDetailsService;
import com.institute.Institue.model.User;

public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);
    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthFilter(JwtService jwtService, CustomUserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    // Don't run this filter for endpoints that should be publicly accessible without JWT
    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        if (path == null) return false;
        // Use contains so we handle context path, trailing slashes, and query params
        if (path.contains("/api/superadmin/seed") || path.contains("/api/superadmin/reset-seed")) return true;
        if (path.startsWith("/api/auth/")) return true;
        if (path.contains("/actuator/health")) return true;
        if (path.startsWith("/api/payments/webhook/")) return true;
        return false;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
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
                            log.debug("Set tenant context to {} for request {} {}", org, request.getMethod(), request.getRequestURI());
                        }

                        // Build Authentication and set SecurityContext so Spring Security knows the user
                        Object sub = claims.get("sub");
                        String principal = sub == null ? null : String.valueOf(sub);

                        Object rolesObj = claims.get("roles");
                        List<String> roles = null;
                        if (rolesObj instanceof List) {
                            roles = ((List<?>) rolesObj).stream().map(Object::toString).collect(Collectors.toList());
                        }

                        if (principal != null) {
                            List<SimpleGrantedAuthority> authorities = roles == null ? List.of() : roles.stream()
                                    .map(r -> new SimpleGrantedAuthority("ROLE_" + r)).collect(Collectors.toList());

                            // Load full User entity as principal when possible
                            Object principalObj = principal;
                            try {
                                User user = (User) userDetailsService.loadUserByUsername(principal);
                                principalObj = user;
                            } catch (Exception e) {
                                // If loading fails, fall back to using the username string as principal
                                log.debug("Could not load full User for principal={}, continuing with username only", principal);
                            }

                            Authentication authentication = new UsernamePasswordAuthenticationToken(principalObj, null, authorities);
                            SecurityContextHolder.getContext().setAuthentication(authentication);
                            log.debug("Authenticated principal={} roles={} for request {} {}", principalObj, roles, request.getMethod(), request.getRequestURI());
                        }
                    }
                } else {
                    log.debug("Invalid JWT token for request {} {}", request.getMethod(), request.getRequestURI());
                }
            } else {
                // Optionally allow X-ORG-ID header for testing/admin flows
                String orgHeader = request.getHeader("X-ORG-ID");
                if (orgHeader != null && !orgHeader.isBlank()) {
                    TenantContext.setCurrentOrg(orgHeader);
                    log.debug("Tenant context set from X-ORG-ID header to {} for request {} {}", orgHeader, request.getMethod(), request.getRequestURI());
                }
            }

            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }
}
