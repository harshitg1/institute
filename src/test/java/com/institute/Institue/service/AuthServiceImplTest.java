package com.institute.Institue.service;

import com.institute.Institue.dto.AuthRequest;
import com.institute.Institue.dto.AuthResponse;
import com.institute.Institue.model.Organization;
import com.institute.Institue.model.Role;
import com.institute.Institue.model.User;
import com.institute.Institue.repository.OrganizationRepository;
import com.institute.Institue.repository.RoleRepository;
import com.institute.Institue.repository.UserRepository;
import com.institute.Institue.security.JwtService;
import com.institute.Institue.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    public void authenticate_existingUser_generatesAccessAndRefreshTokens() {
        // 1. Arrange
        String email = "bob@example.com";
        String rawPassword = "secret-password";
        String encodedPassword = "encoded-hash";
        UUID orgId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();

        Organization org = Organization.builder()
                .id(orgId)
                .name("Test Org")
                .build();

        Role role = Role.builder()
                .id(roleId)
                .role(com.institute.Institue.model.enums.UserRole.SUPER_ADMIN)
                .build();

        User user = User.builder()
                .id(UUID.randomUUID())
                .email(email)
                .password(encodedPassword)
                .role(role)
                .organization(org)
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(true);
        when(jwtService.generateAccessToken(eq(email), anyString(), anyString(), anyList()))
                .thenReturn("mock-access-token");
        when(jwtService.generateRefreshToken(email))
                .thenReturn("mock-refresh-token");

        // 2. Act
        AuthResponse resp = authService.authenticate(new AuthRequest(email, rawPassword));

        // 3. Assert
        assertNotNull(resp);
        assertEquals("mock-access-token", resp.getAccessToken());
        assertEquals("mock-refresh-token", resp.getRefreshToken());
        assertEquals("SUPER_ADMIN", resp.getRole());
        assertEquals(orgId.toString(), resp.getOrganizationId());
    }

    @Test
    public void authenticate_invalidPassword_throwsException() {
        String email = "bob@example.com";
        Role role = Role.builder().id(UUID.randomUUID()).role(com.institute.Institue.model.enums.UserRole.STUDENT).build();
        User user = User.builder().email(email).password("hashed").role(role).build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        assertThrows(RuntimeException.class, () -> {
            authService.authenticate(new AuthRequest(email, "wrong-password"));
        });
    }
}