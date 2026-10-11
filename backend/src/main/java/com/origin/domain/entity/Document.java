package com.origin.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "documents")
@Data
@NoArgsConstructor
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String fileName;

    private String mimeType;
    private Long fileSizeBytes;

    @Enumerated(EnumType.STRING)
    private IngestionStatus status = IngestionStatus.PENDING;

    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DocumentChunk> chunks = new ArrayList<>();

    @CreationTimestamp
    private Instant uploadedAt;

    public enum IngestionStatus { PENDING, PROCESSING, COMPLETED, FAILED }
}

