# SYSTEM DIRECTIVE & IMPLEMENTATION PROMPT

> **Instructions for the AI Assistant**: You are tasked with implementing **RamLaal**, an autonomous personal AI assistant and workflow orchestrator. Read this prompt carefully and adhere strictly to all directives, constraints, and execution phases.

---

## 1. Project Mission & Context

You are developing **RamLaal**, a stateful personal AI assistant capable of:
1. **User Memory & Context**: Recalls prior conversations and durable personal facts.
2. **Query Expansion**: Expands user prompts into 2–3 targeted queries for effective retrieval.
3. **Document RAG**: Ingests and searches local documents and uploads using vector search.
4. **Tool Execution Engine**: Dispatches actions across 5 core tools:
   - `Calendar`: Create, list, delete schedule events.
   - `Task`: Manage todo items, priorities, and statuses.
   - `Reminder`: Set persistent time-based alerts.
   - `Alarm`: Configure system triggers.
   - `Scheduler`: Configure recurring cron jobs.
5. **Learning Layer**: Analyzes completed interactions to extract durable user facts and preferences, deduplicating and persisting them into long-term memory.
6. **Frontend Dashboard**: A responsive React interface for chatting, viewing calendar/tasks, and inspecting memory.

All architecture decisions and plans are documented in:
- [`PROJECT_PLAN.md`](./PROJECT_PLAN.md)
- [`AGENTS.md`](./AGENTS.md)
- [`.agents/memory/`](./.agents/memory/)

---

## 2. Core Technical Stack & Rules

### Tech Stack
- **Backend**: Java 21 LTS, Spring Boot 3.3+, Spring AI 1.0+ (`spring-ai-azure-openai-spring-boot-starter`).
- **LLM & Embeddings**: Azure OpenAI (Chat: `gpt-4o`/`gpt-4o-mini`, Embeddings: `text-embedding-3-small`).
- **Database & Vectors**: PostgreSQL 16 with `pgvector` (via Docker Compose).
- **Tool Mechanism**: Spring AI `@Tool` function calling with strong DTO input validation.
- **Frontend**: React 18+ (Vite, TypeScript, Tailwind CSS).
- **Layout**: Maven Monorepo with `backend/` and `frontend/` directories.

### Engineering Rules
1. **No Reinventing the Wheel**: If a task can be solved reliably using a standard library (e.g., Spring AI starters, Spring Data JPA, `pgvector-java`, JobRunr/Spring `@Scheduled`), use the library rather than writing custom, complex boilerplate.
2. **Clean & Concise Code**: Write idiomatic, modern, strongly typed Java and TypeScript. Avoid redundant comments or obvious restatements—let the code be clean and self-documenting. Preserve only meaningful architectural notes.
3. **Fail Fast & Validate**: Strictly validate tool inputs (ISO-8601 timestamps, non-empty fields, timezone safety). Contain errors gracefully so a failed tool call returns an informative message to the LLM rather than crashing the runtime.

---

## 3. Communication & Clarification Protocols

1. **Clarification Over Guessing**: If you encounter an ambiguous requirement, missing environment variable, unexpected error, or architectural choice not covered in `PROJECT_PLAN.md`, **stop and ask the user directly** before proceeding.
2. **Stage-by-Stage Reporting**: At the conclusion of every implementation phase:
   - Provide a concise summary of what was built and verified.
   - State the files created or modified.
   - State the immediate next phase you are starting.
3. **Memory Bank Maintenance**: After completing significant milestones, update `.agents/memory/activeContext.md` and `.agents/memory/progress.md` to keep the project memory synced.

---

## 4. Step-by-Step Implementation Sequence

Execute the following phases in exact sequential order:

### Phase 0: Repository Hygiene & Foundation
1. Verify or initialize Git repository tracking.
2. Create a comprehensive `.gitignore` covering Java/Maven (`target/`, `.mvn/wrapper/maven-wrapper.jar`), Node/React (`node_modules/`, `dist/`), and environment secrets (`.env`, `*.key`).
3. Create `.env.example` defining all necessary environment variables:
   - `AZURE_OPENAI_API_KEY`
   - `AZURE_OPENAI_ENDPOINT`
   - `AZURE_OPENAI_CHAT_DEPLOYMENT`
   - `AZURE_OPENAI_EMBEDDING_DEPLOYMENT`
   - `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD`, `POSTGRES_PORT`
4. Create `docker-compose.yml` for PostgreSQL 16 with `pgvector/pgvector:pg16` and persistent volume mounts.

### Phase 1: Backend Scaffolding (Spring Boot 3.3+)
1. Scaffold `backend/` with Maven (`pom.xml`) and Java 21.
2. Add dependencies:
   - `spring-boot-starter-web`
   - `spring-boot-starter-data-jpa`
   - `spring-ai-azure-openai-spring-boot-starter` (Spring AI 1.0.0-M1 or latest milestone)
   - `org.postgresql:postgresql`
   - `com.pgvector:pgvector`
3. Configure `backend/src/main/resources/application.yml` referencing environment variables.
4. Verify application startup and PostgreSQL connection.

### Phase 2: Relational Schemas & Domain Entities
1. Create JPA entities and repositories:
   - `ChatSession` & `ChatMessage` (Session tracking)
   - `UserMemory` (id, user_id, fact_text, embedding `vector(1536)`, category, timestamps)
   - `Document` & `DocumentChunk` (id, document_id, content, embedding `vector(1536)`)
   - Tool entities: `CalendarEvent`, `TaskItem`, `ReminderItem`, `AlarmItem`, `ScheduledJobItem`
2. Create database migration script or JPA schema initialization ensuring `CREATE EXTENSION IF NOT EXISTS vector;` runs first.

### Phase 3: The 5 Core Action Tools (Spring AI `@Tool`)
1. Implement and test typed `@Tool` components:
   - `CalendarTool`: `createCalendarEvent`, `listCalendarEvents`, `deleteCalendarEvent`
   - `TaskTool`: `createTask`, `listTasks`, `completeTask`, `deleteTask`
   - `ReminderTool`: `setReminder`, `listActiveReminders`, `cancelReminder`
   - `AlarmTool`: `setAlarm`, `dismissAlarm`
   - `SchedulerTool`: `scheduleJob`, `listJobs`
2. Implement `BackgroundNotificationDispatcher` using Spring `@Scheduled` to check pending reminders and alarms every 30 seconds.

### Phase 4: Query Expansion & Hybrid RAG Pipeline
1. Implement `QueryExpansionService`: Uses Azure OpenAI to transform the user's latest query into 2–3 search formulations.
2. Implement `DocumentIngestionService`:
   - Startup directory scanner for `./data/documents/` (PDF, TXT, MD).
   - Multipart file upload REST endpoint (`POST /api/documents/upload`).
   - Token chunking (500 tokens with 50-token overlap) + Azure OpenAI embedding generation.
3. Implement `VectorRetrievalService`: Cosine distance search (`<=>`) in PostgreSQL with deduplication and top-k selection.

### Phase 5: Memory Bank & Learning Layer
1. Implement `UserMemoryService`: Fetches user preferences and historical facts matching the active topic.
2. Implement `LearningLayerService`: Post-interaction evaluation prompt extracting new durable facts from the conversation; deduplicates and upserts into `user_memories`.

### Phase 6: Orchestration Engine & Streaming APIs
1. Implement `AgentOrchestratorService`: Assembles the pipeline:
   - `Context Ingestion` -> `Query Expansion` -> `RAG Retrieval` -> `Azure OpenAI Reasoning + @Tool Dispatch` -> `Learning Layer Extraction`.
2. Expose REST endpoints:
   - `POST /api/chat/stream`: Server-Sent Events (SSE) streaming chat responses.
   - `GET /api/dashboard`: Aggregated calendar, tasks, reminders, alarms.
   - `GET /api/memories` & `DELETE /api/memories/{id}`: Memory management.

### Phase 7: React Frontend Application
1. Scaffold `frontend/` using Vite + React + TypeScript + Tailwind CSS.
2. Build UI components:
   - Interactive Chat panel with streaming responses and tool execution callouts.
   - Productivity Dashboard (Calendar events, Todo list, Active reminders/alarms).
   - Memory Inspector (visualize and delete stored personal facts).

### Phase 8: Verification & End-to-End Walkthrough
1. Run backend unit and integration tests.
2. Execute a multi-turn verification prompt:
   - *"Schedule a project sync on Thursday at 4 PM, remind me 10 minutes prior, and note that I prefer meetings after 2 PM."*
3. Verify that the event is saved in `calendar_events`, the reminder is queued, and the preference is recorded in `user_memories`.
4. Update `.agents/memory/progress.md` with completed milestones.

---

## 5. Execution Kick-Off

Begin immediately with **Phase 0** and **Phase 1**:
1. Check git status, setup `.gitignore`, `.env.example`, and `docker-compose.yml`.
2. Scaffold the Maven `backend/` project.
3. Report progress to the user upon finishing Phase 0 and Phase 1, and request their Azure OpenAI credentials before proceeding to the live tool tests.
