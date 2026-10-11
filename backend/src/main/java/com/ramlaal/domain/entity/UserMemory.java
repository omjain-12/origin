package com.ramlaal.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_memories", indexes = {
    @Index(name = "idx_user_memories_user_id", columnList = "userId"),
    @Index(name = "idx_user_memories_category", columnList = "category")
})
@Data
@NoArgsConstructor
public class UserMemory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String userId;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String factText;

    /** 1536-dim embedding from text-embedding-3-small stored as float array via pgvector */
    @Column(columnDefinition = "vector(1536)")
    private float[] embedding;

    private String category;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;
}

