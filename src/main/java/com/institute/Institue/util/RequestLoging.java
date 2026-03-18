package com.institute.Institue.util;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
public class RequestLoging extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String traceId = UUID.randomUUID().toString();

        long start = System.currentTimeMillis();

        log.info("[{}] Incoming Request {} {}", traceId, request.getMethod(), request.getRequestURI());

        filterChain.doFilter(request, response);

        long duration = System.currentTimeMillis() - start;

        log.info("[{}] Response {} ({} ms)", traceId, response.getStatus(), duration);
    }
}