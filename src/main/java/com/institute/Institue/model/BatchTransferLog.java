package com.institute.Institue.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "batch_transfer_log", indexes = {
        @Index(name = "idx_btl_student_id", columnList = "student_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchTransferLog {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_batch_id", nullable = false)
    private Batch fromBatch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_batch_id", nullable = false)
    private Batch toBatch;

    @Column(length = 500)
    private String reason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transferred_by")
    private User transferredBy;

    @CreationTimestamp
    @Column(name = "transferred_at", updatable = false)
    private Instant transferredAt;
}
