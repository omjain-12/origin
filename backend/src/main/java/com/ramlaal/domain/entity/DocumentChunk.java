package com.ramlaal.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "document_chunks")
@Data
@NoArgsConstructor
public class DocumentChunk {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    /** Chunk sequence number within the parent document */
    private int chunkIndex;

    /** 1536-dim embedding for similarity retrieval */
    @Column(columnDefinition = "vector(1536)")
    private float[] embedding;
}
