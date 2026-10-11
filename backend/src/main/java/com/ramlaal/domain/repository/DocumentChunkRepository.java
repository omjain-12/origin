package com.ramlaal.domain.repository;

import com.ramlaal.domain.entity.DocumentChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DocumentChunkRepository extends JpaRepository<DocumentChunk, UUID> {

    /** Top-k cosine similarity search across all document chunks */
    @Query(value = """
        SELECT * FROM document_chunks
        ORDER BY embedding <=> CAST(:embedding AS vector)
        LIMIT :k
        """, nativeQuery = true)
    List<DocumentChunk> findTopKBySemanticSimilarity(
        @Param("embedding") String embedding,
        @Param("k") int k
    );
}
