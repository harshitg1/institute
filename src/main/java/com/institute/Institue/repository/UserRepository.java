package com.institute.Institue.repository;

import com.institute.Institue.model.User;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository {
    Optional<User> findById(String id);
    Optional<User> findByUsername(String username);
    List<User> findAll();
}

