package com.institute.Institue.repository;

import com.institute.Institue.model.PaymentOrder;
import com.institute.Institue.model.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentOrderRepository extends JpaRepository<PaymentOrder, UUID> {

    @Query("SELECT po FROM PaymentOrder po " +
            "JOIN FETCH po.course " +
            "JOIN FETCH po.student " +
            "WHERE po.organization.id = :orgId " +
            "ORDER BY po.createdAt DESC")
    List<PaymentOrder> findByOrganizationId(@Param("orgId") UUID orgId);

    @Query("SELECT po FROM PaymentOrder po " +
            "JOIN FETCH po.course " +
            "WHERE po.student.id = :studentId " +
            "ORDER BY po.createdAt DESC")
    List<PaymentOrder> findByStudentId(@Param("studentId") UUID studentId);

    Optional<PaymentOrder> findByProviderOrderId(String providerOrderId);

    @Query("SELECT po FROM PaymentOrder po " +
            "JOIN FETCH po.course " +
            "JOIN FETCH po.student " +
            "WHERE po.organization.id = :orgId AND po.status = :status")
    List<PaymentOrder> findByOrganizationIdAndStatus(
            @Param("orgId") UUID orgId, @Param("status") PaymentStatus status);

    boolean existsByStudent_IdAndCourse_IdAndStatus(UUID studentId, UUID courseId, PaymentStatus status);

    // --- New fetch methods to safely load associations before updates ---
    @Query("SELECT po FROM PaymentOrder po JOIN FETCH po.course JOIN FETCH po.student WHERE po.id = :id")
    Optional<PaymentOrder> findByIdWithAssociations(@Param("id") UUID id);

    @Query("SELECT po FROM PaymentOrder po JOIN FETCH po.course JOIN FETCH po.student WHERE po.providerOrderId = :providerOrderId")
    Optional<PaymentOrder> findByProviderOrderIdWithAssociations(@Param("providerOrderId") String providerOrderId);
}
