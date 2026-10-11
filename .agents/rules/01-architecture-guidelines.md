# Architecture Guidelines for RamLaal

This rule defines the architectural boundaries and responsibilities for components within the RamLaal assistant project.

---

## 1. High-Level Modular Decomposition

RamLaal is partitioned into six decoupled layers:

```
[User / Client]
       │
       ▼
1. [API & Session Manager]
       │
       ├──► 2. [Context & Retrieval Pipeline] (Query expansion, hybrid search, RAG)
       │
       ├──► 3. [Agent Orchestrator] (Intent routing, state graph, planner)
       │         │
       │         ├──► 4. [Tool Registry & Executor] (Calendar, tasks, reminders, alarms)
       │         │
       │         └──► 5. [Memory & Learning Service] (Working memory, fact extraction, updates)
       │
       ▼
6. [Storage & Background Jobs] (PostgreSQL/pgvector, JobRunr / Celery, ChromaDB / SQLite)
```

---

## 2. Component Responsibilities

### Layer 1: API & Session Manager
- Handles HTTP/WebSocket endpoints, user authentication, and session identifiers.
- Manages conversational turn history and active session metadata.
- Sanitizes inbound prompts and formats outbound streaming responses.

### Layer 2: Context & Retrieval Pipeline
- **Query Expansion**: Generates alternative search formulations for complex queries.
- **Hybrid Retrieval**: Combines dense vector search (embeddings) with sparse lexical search (BM25).
- **Reranking**: Reranks top-$k$ results for relevance before passing to the model context.
- **Context Assembly**: Blends user profile, relevant memory facts, and retrieved documents into a token-budgeted prompt.

### Layer 3: Agent Orchestrator
- Implements stateful workflow orchestration (State Machine or Graph pattern, e.g., Spring AI / LangGraph).
- Determines whether a request requires conversational answer, tool execution, or multi-step sub-tasks.
- Enforces loop limits, retry policies, and fallback actions.

### Layer 4: Tool Registry & Executor
- Strictly typed input and output contracts for all tools (e.g., JSON Schema / DTOs / Pydantic models).
- Isolates side effects. Tools must validate dates, timezones, and authorization before taking real-world actions.
- Returns structured results to the agent with clear success/failure indicators.

### Layer 5: Memory & Learning Service
- **Working Memory**: In-flight state across conversational turns within the session.
- **Long-Term Memory**: Durable factual repository (user preferences, facts, recurring schedules).
- **Learning Pipeline**: Post-interaction extraction of novel facts; deduplicates against existing records and resolves conflicts before persisting.

### Layer 6: Storage & Background Jobs
- Relational schema for tasks, reminders, events, and chat sessions.
- Vector store with embedding indexing.
- Reliable background job runner for recurring tasks and alarms that survives process restarts.
