package com.ramlaal.service;

import com.ramlaal.domain.entity.UserMemory;
import com.ramlaal.domain.repository.UserMemoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserMemoryService {

    private final UserMemoryRepository userMemoryRepository;
    private final EmbeddingModel embeddingModel;

    @Value("${ramlaal.memory.user-id:default-user}")
    private String userId;

    public List<UserMemory> findRelevantMemories(String query, int topK) {
        try {
            float[] embedding = embeddingModel.embed(query);
            String embeddingStr = VectorRetrievalService.floatArrayToVectorString(embedding);
            return userMemoryRepository.findTopKBySemanticSimilarity(userId, embeddingStr, topK);
        } catch (Exception e) {
            log.warn("Memory retrieval failed: {}", e.getMessage());
            return List.of();
        }
    }

    public UserMemory saveMemory(String factText, String category) {
        try {
            float[] embedding = embeddingModel.embed(factText);
            UserMemory memory = new UserMemory();
            memory.setUserId(userId);
            memory.setFactText(factText);
            memory.setEmbedding(embedding);
            memory.setCategory(category);
            return userMemoryRepository.save(memory);
        } catch (Exception e) {
            log.error("Failed to save memory: {}", e.getMessage());
            throw new RuntimeException("Memory persistence failed", e);
        }
    }

    public List<UserMemory> getAllMemories() {
        return userMemoryRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public void deleteMemory(UUID id) {
        userMemoryRepository.deleteById(id);
    }
}
