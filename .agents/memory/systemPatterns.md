# System Patterns: Origin

## Architectural Patterns

### 1. Supervisor / State Graph Orchestration
- Uses a deterministic state graph or coordinator pattern to route user requests:
  - `Query Rewriter` -> `Context Fetcher` (History + Memory + RAG) -> `Agent Router` -> (`Tool Executor` OR `Direct Responder`) -> `Learning Extractor` -> `Response Generator`.
- Tool calls are decoupled from LLM chat endpoints via an explicit tool execution engine.

### 2. Dual-Tier Memory Pattern
- **Short-Term (Session) Memory**: Holds recent chat conversation messages within a sliding window or summary token budget.
- **Long-Term (Durable) Memory**:
  - Semantic vector storage for unstructured notes and user profile facts.
  - Relational tables for structured entities (reminders, alarms, calendar events).
  - Explicit learning node runs asynchronously to detect durable facts without polluting memory with chat trivia.

### 3. Tool Engine Pattern
- All tools adhere to a unified interface:
  - `name`: Unique tool identifier.
  - `description`: LLM-facing instructions on when and how to call the tool.
  - `inputSchema`: Strongly-typed JSON schema (or DTO/Pydantic class).
  - `execute(input)`: Validates inputs, executes operation, captures errors safely, and returns standard output envelope.
- Critical real-world actions (e.g., deleting events, setting system alarms) include validation guards and confirmation checkpoints.

### 4. Hybrid Retrieval Pattern
- Ingestion: Chunking -> Vector Embedding -> Inverted Index.
- Query: User query -> Query Expansion (generating 2-3 focused variations) -> Parallel vector search + BM25 lexical search -> Reciprocal Rank Fusion (RRF) -> Reranking -> Top-$k$ context assembly.
