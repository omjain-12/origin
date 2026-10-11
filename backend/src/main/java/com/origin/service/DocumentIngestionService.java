package com.origin.service;

import com.origin.domain.entity.Document;
import com.origin.domain.entity.DocumentChunk;
import com.origin.domain.repository.DocumentChunkRepository;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class DocumentIngestionService {

    private static final Logger log = LoggerFactory.getLogger(DocumentIngestionService.class);

    private final EmbeddingModel embeddingModel;
    private final DocumentChunkRepository documentChunkRepository;
    private final EntityManager entityManager;

    @Value("${Origin.documents.scan-directory:./data/documents}")
    private String scanDirectory;

    @Value("${Origin.retrieval.chunk-size:500}")
    private int chunkSize;

    @Value("${Origin.retrieval.chunk-overlap:50}")
    private int chunkOverlap;

    public DocumentIngestionService(EmbeddingModel embeddingModel,
                                    DocumentChunkRepository documentChunkRepository,
                                    EntityManager entityManager) {
        this.embeddingModel = embeddingModel;
        this.documentChunkRepository = documentChunkRepository;
        this.entityManager = entityManager;
    }

    @PostConstruct
    public void scanAndIngestDirectory() {
        Path dir = Path.of(scanDirectory);
        if (!Files.exists(dir)) {
            log.info("Document scan directory does not exist yet: {}", dir);
            return;
        }
        try (var stream = Files.walk(dir, 1)) {
            stream.filter(p -> !Files.isDirectory(p))
                .filter(p -> isSupported(p.getFileName().toString()))
                .forEach(p -> {
                    try {
                        ingestFile(p.getFileName().toString(), Files.readAllBytes(p));
                        log.info("Auto-ingested: {}", p.getFileName());
                    } catch (Exception e) {
                        log.error("Failed to ingest {}: {}", p, e.getMessage());
                    }
                });
        } catch (IOException e) {
            log.error("Failed to scan documents directory: {}", e.getMessage());
        }
    }

    public Document ingestUpload(MultipartFile file) throws IOException {
        return ingestFile(file.getOriginalFilename(), file.getBytes());
    }

    @Transactional
    public Document ingestFile(String fileName, byte[] bytes) {
        String text = new String(bytes, StandardCharsets.UTF_8);
        List<String> chunks = chunk(text);

        Document doc = new Document();
        doc.setFileName(fileName);
        doc.setFileSizeBytes((long) bytes.length);
        doc.setStatus(Document.IngestionStatus.PROCESSING);
        entityManager.persist(doc);
        entityManager.flush();

        int idx = 0;
        for (String chunkText : chunks) {
            try {
                float[] embedding = embeddingModel.embed(chunkText);
                DocumentChunk chunk = new DocumentChunk();
                chunk.setDocument(doc);
                chunk.setContent(chunkText);
                chunk.setChunkIndex(idx++);
                chunk.setEmbedding(embedding);
                documentChunkRepository.save(chunk);
            } catch (Exception e) {
                log.error("Embedding failed for chunk {} of {}: {}", idx, fileName, e.getMessage());
            }
        }

        doc.setStatus(Document.IngestionStatus.COMPLETED);
        entityManager.merge(doc);
        return doc;
    }

    private List<String> chunk(String text) {
        String[] words = text.split("\\s+");
        List<String> chunks = new ArrayList<>();
        int start = 0;
        while (start < words.length) {
            int end = Math.min(start + chunkSize, words.length);
            chunks.add(String.join(" ", java.util.Arrays.copyOfRange(words, start, end)));
            start += chunkSize - chunkOverlap;
        }
        return chunks;
    }

    private boolean isSupported(String name) {
        String lower = name.toLowerCase();
        return lower.endsWith(".txt") || lower.endsWith(".md");
    }
}
