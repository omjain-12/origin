package com.origin.service;

import com.origin.domain.entity.DocumentChunk;
import com.origin.domain.repository.DocumentChunkRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class VectorRetrievalService {

    private final EmbeddingModel embeddingModel;
    private final DocumentChunkRepository documentChunkRepository;
    private final UserMemoryService userMemoryService;

    @Value("${Origin.retrieval.top-k:5}")
    private int topK;

    @Value("${Origin.memory.user-id:default-user}")
    private String userId;

    /**
     * Retrieves top-k document chunks semantically matching the given queries,
     * deduplicates by chunk ID, and returns concatenated context text.
     */
    public String retrieveContext(List<String> queries) {
        Set<UUID> seen = new LinkedHashSet<>();
        List<DocumentChunk> allChunks = new ArrayList<>();

        for (String query : queries) {
            try {
                float[] embedding = embeddingModel.embed(query);
                String embeddingStr = floatArrayToVectorString(embedding);
                List<DocumentChunk> chunks = documentChunkRepository
                    .findTopKBySemanticSimilarity(embeddingStr, topK);
                for (DocumentChunk chunk : chunks) {
                    if (seen.add(chunk.getId())) {
                        allChunks.add(chunk);
                    }
                }
            } catch (Exception e) {
                log.warn("Vector retrieval failed for query '{}': {}", query, e.getMessage());
            }
        }

        if (allChunks.isEmpty()) return "";

        return allChunks.stream()
            .limit(topK)
            .map(DocumentChunk::getContent)
            .collect(Collectors.joining("\n---\n"));
    }

    /** Converts float[] to pgvector literal string format e.g. "[0.1,0.2,...]" */
    public static String floatArrayToVectorString(float[] arr) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < arr.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(arr[i]);
        }
        sb.append("]");
        return sb.toString();
    }
}

