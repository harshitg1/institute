package com.institute.Institue.util;

import com.institute.Institue.model.User;
import com.institute.Institue.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SecurityUtils {

    private final UserRepository userRepository;

    public SecurityUtils(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Resolve the admin principal when @AuthenticationPrincipal could be null.
     */
    public Optional<User> resolveAdmin(User admin) {
        if (admin != null) return Optional.of(admin);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return Optional.empty();

        Object principal = auth.getPrincipal();
        if (principal == null) return Optional.empty();

        if (principal instanceof User) return Optional.of((User) principal);

        if (principal instanceof UserDetails) {
            String username = ((UserDetails) principal).getUsername();
            return userRepository.findByEmail(username);
        }

        String username = principal instanceof String ? (String) principal : principal.toString();
        if (username == null || username.isBlank()) return Optional.empty();
        return userRepository.findByEmail(username);
    }
}
