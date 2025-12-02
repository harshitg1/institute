package com.institute.Institue.config;
}
    }
        return new BCryptPasswordEncoder();
    public PasswordEncoder passwordEncoder() {
    @Bean

    }
        return http.build();
            .authorizeHttpRequests().anyRequest().permitAll();
            .csrf().disable()
        http
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    @Bean

public class SecurityConfig {
@Configuration

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;


