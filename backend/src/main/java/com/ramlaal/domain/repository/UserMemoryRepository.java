package com.ramlaal.domain.repository;

import com.ramlaal.domain.entity.UserMemory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserMemoryRepository extends JpaRepository<UserMemory, UUID> {

    List<UserMemory> findByUserIdOrderByCreatedAtDesc(String userId);

    /** Top-k cosine similarity search using pgvector <=> operator */
    @Query(value = """
        SELECT * FROM user_memories
        WHERE user_id = :userId
        ORDER BY embedding <=> CAST(:embedding AS vector)
        LIMIT :k
        """, nativeQuery = true)
    List<UserMemory> findTopKBySemanticSimilarity(
        @Param("userId") String userId,
        @Param("embedding") String embedding,
        @Param("k") int k
    );
}
