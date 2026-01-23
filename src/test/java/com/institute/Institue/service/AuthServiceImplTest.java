package com.institute.Institue.service;

import com.institute.Institue.dto.AuthRequest;
import com.institute.Institue.dto.AuthResponse;
import com.institute.Institue.model.Role;
import com.institute.Institue.model.User;
import com.institute.Institue.repository.UserRepository;
import com.institute.Institue.security.JwtService;
import com.institute.Institue.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

public class AuthServiceImplTest {

    @Test
    public void authenticate_existingUser_generatesToken() {
        UserRepository mockRepo = Mockito.mock(UserRepository.class);
        JwtService realJwt = new JwtService();

        Role r = Role.builder().id(UUID.randomUUID()).name("SUPER_ADMIN").build();
        User u = User.builder().id(UUID.randomUUID()).email("bob@example.com").password("hashed").roles(Set.of(r)).build();

        Mockito.when(mockRepo.findByEmail("bob@example.com")).thenReturn(Optional.of(u));

        AuthServiceImpl svc = new AuthServiceImpl(mockRepo, realJwt);
        AuthResponse resp = svc.authenticate(new AuthRequest("bob@example.com", "secret"));

        assertNotNull(resp);
        assertNotNull(resp.getToken());
    }
}
