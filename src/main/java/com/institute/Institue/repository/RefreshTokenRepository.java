package com.institute.Institue.repository;

import com.institute.Institue.model.RefreshToken;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository {
    Optional<RefreshToken> findByToken(String token);
    void deleteById(String id);
}

