package com.origin.config;

import org.springframework.ai.document.Document;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**
 * High-performance deterministic in-memory vectorizer producing 1536-dimensional L2-normalized embeddings.
 * Eliminates dependency on remote /v1/embeddings endpoints not supported by LLM-only providers.
 */
@Component
@Primary
public class LocalDeterministicEmbeddingModel implements EmbeddingModel {

    private static final int DIMENSIONS = 1536;

    @Override
    public float[] embed(String text) {
        float[] vector = new float[DIMENSIONS];
        if (text == null || text.isBlank()) {
            return vector;
        }

        String[] tokens = text.toLowerCase().split("\\W+");
        for (String token : tokens) {
            if (token.isBlank()) continue;
            try {
                MessageDigest md = MessageDigest.getInstance("MD5");
                byte[] hash = md.digest(token.getBytes(StandardCharsets.UTF_8));
                for (int i = 0; i < hash.length; i++) {
                    int idx = Math.abs((hash[i] * 31 + i * 17 + token.hashCode())) % DIMENSIONS;
                    vector[idx] += 1.0f;
                }
            } catch (NoSuchAlgorithmException ignored) {
            }
        }

        // L2-normalize vector for cosine similarity
        float norm = 0.0f;
        for (float v : vector) {
            norm += v * v;
        }
        if (norm > 0) {
            float sqrtNorm = (float) Math.sqrt(norm);
            for (int i = 0; i < vector.length; i++) {
                vector[i] /= sqrtNorm;
            }
        }
        return vector;
    }

    @Override
    public float[] embed(Document document) {
        return embed(document.getFormattedContent(MetadataMode.NONE));
    }

    @Override
    public List<float[]> embed(List<String> texts) {
        List<float[]> results = new ArrayList<>(texts.size());
        for (String t : texts) {
            results.add(embed(t));
        }
        return results;
    }

    @Override
    public EmbeddingResponse call(EmbeddingRequest request) {
        List<Embedding> embeddings = new ArrayList<>();
        int index = 0;
        for (String instruction : request.getInstructions()) {
            embeddings.add(new Embedding(embed(instruction), index++));
        }
        return new EmbeddingResponse(embeddings);
    }

    @Override
    public int dimensions() {
        return DIMENSIONS;
    }
}
