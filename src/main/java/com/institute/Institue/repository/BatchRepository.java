package com.institute.Institue.repository;

import com.institute.Institue.model.Batch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BatchRepository extends JpaRepository<Batch, UUID> {

    List<Batch> findByOrganization_IdAndActiveTrue(UUID organizationId);

    List<Batch> findByOrganization_Id(UUID organizationId);

    @Query("SELECT b FROM Batch b " +
            "LEFT JOIN FETCH b.instructor " +
            "LEFT JOIN FETCH b.organization " +
            "WHERE b.id = :id")
    Optional<Batch> findByIdWithDetails(@Param("id") UUID id);

    boolean existsByNameAndOrganization_Id(String name, UUID organizationId);
}
