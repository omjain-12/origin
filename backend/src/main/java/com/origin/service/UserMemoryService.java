package com.origin.service;

import com.origin.domain.entity.UserMemory;
import com.origin.domain.repository.UserMemoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class UserMemoryService {

    private static final Logger log = LoggerFactory.getLogger(UserMemoryService.class);

    private final UserMemoryRepository userMemoryRepository;
    private final EmbeddingModel embeddingModel;
    private final org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @Value("${Origin.memory.user-id:default-user}")
    private String userId;

    public UserMemoryService(UserMemoryRepository userMemoryRepository, EmbeddingModel embeddingModel, org.springframework.jdbc.core.JdbcTemplate jdbcTemplate) {
        this.userMemoryRepository = userMemoryRepository;
        this.embeddingModel = embeddingModel;
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<UserMemory> findRelevantMemories(String query, int topK) {
        try {
            if (userMemoryRepository.count() == 0) {
                return List.of();
            }
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
            UserMemory saved = userMemoryRepository.save(memory);
            try {
                String embeddingStr = VectorRetrievalService.floatArrayToVectorString(embedding);
                jdbcTemplate.update("UPDATE user_memories SET embedding = CAST(? AS vector) WHERE id = ?", embeddingStr, saved.getId());
            } catch (Exception ex) {
                log.warn("Could not update vector embedding for memory {}: {}", saved.getId(), ex.getMessage());
            }
            return saved;
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
