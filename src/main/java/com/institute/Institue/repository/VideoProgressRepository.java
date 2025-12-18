package com.institute.Institue.repository;

import com.institute.Institue.model.VideoProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface VideoProgressRepository extends JpaRepository<VideoProgress, UUID> {
    // Reverted public-specific methods. Keep basic CRUD.
}

