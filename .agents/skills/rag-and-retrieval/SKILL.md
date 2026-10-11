---
name: rag-and-retrieval
description: >-
  Guides the design, chunking, indexing, hybrid retrieval (vector + BM25), query expansion,
  and context assembly for the personal assistant knowledge base.
---

# RAG & Hybrid Retrieval Skill

Use this skill when developing or tuning document ingestion, search pipelines, or knowledge retrieval for Origin.

## Procedure

1. **Query Expansion**:
   - For user inputs containing complex or ambiguous references, generate 2-3 focused alternative search queries.
   - Example: *"What did I discuss with Sarah last Tuesday?"* -> `[ "notes meeting Sarah Tuesday", "Sarah project discussion", "transcript Sarah" ]`.

2. **Parallel Hybrid Search**:
   - Query the dense vector store (e.g., PostgreSQL `pgvector` with cosine distance).
   - Query the lexical index (BM25 or PostgreSQL full-text search `tsvector`).
   - Merge results using Reciprocal Rank Fusion (RRF):
     $$RRF\_score(d) = \sum_{m \in M} \frac{1}{60 + rank_m(d)}$$

3. **Reranking & Deduplication**:
   - Deduplicate documents by ID or content hash.
   - Apply a cross-encoder reranker (or LLM scoring) to top-20 candidates to select top-5 most relevant chunks.

4. **Token-Budget Context Assembly**:
   - Format chunks into a clean context block with source identifiers and timestamps.
   - Ensure retrieved context does not exceed 30% of the active context budget to leave ample room for tool execution and conversation history.
