# PROJECT_PLAN.md: RamLaal Autonomous AI Assistant

This document defines the finalized architecture and step-by-step implementation blueprint for **RamLaal**, an autonomous personal AI assistant built with **Java 21, Spring Boot 3.3+, Spring AI 1.0+, Azure OpenAI, PostgreSQL (pgvector), and React**.

---

## 1. Confirmed Architectural Decisions

| Area | Decision | Details |
| :--- | :--- | :--- |
| **Backend Framework** | Spring Boot 3.3+ (Java 21 LTS) | Spring AI Azure OpenAI Starter, Spring Data JPA, Spring Web |
| **LLM Provider** | Azure OpenAI | Chat completion (`gpt-4o` / `gpt-4o-mini`), Function Calling (`@Tool`), Embeddings (`text-embedding-3-small`) |
| **Database & Vector Store** | PostgreSQL 16 + `pgvector` | Managed locally via `docker-compose.yml` for unified relational & vector storage |
| **Tool Execution Engine** | Internal Database + Scheduler | Dedicated relational tables with persistent scheduler (JobRunr / Spring `@Scheduled`) surviving restarts |
| **RAG Ingestion** | Hybrid (Local Folder + UI Upload) | Auto-scans `./data/documents` on startup & supports multipart file uploads via React UI |
| **Monorepo Layout** | Maven Monorepo | `backend/` (Spring Boot + Maven) and `frontend/` (React + Vite + TypeScript) |

---

## 2. End-to-End Execution Flow

```
                                 [User Request]
                                       │
                                       ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│ Step 1: Context & Memory Ingestion                                          │
│ - Ingest current prompt & session history (sliding token window)            │
│ - Query `user_memories` table for semantic facts about the user             │
└──────────────────────────────────────┬──────────────────────────────────────┘
                                       │
                                       ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│ Step 2: Query Expansion Node                                                │
│ - Calls Azure OpenAI to generate 2–3 targeted alternative search queries    │
└──────────────────────────────────────┬──────────────────────────────────────┘
                                       │
                                       ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│ Step 3: Document Retrieval Node (RAG)                                       │
│ - Computes embeddings for all queries                                       │
│ - Performs cosine distance search in `document_chunks` (pgvector)           │
│ - Merges, deduplicates, and selects top-k relevant context chunks           │
└──────────────────────────────────────┬──────────────────────────────────────┘
                                       │
                                       ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│ Step 4: LLM Reasoning & Tool Execution                                      │
│ - System prompt assembled: Base persona + User Memory + Retrieved Chunks    │
│ - Azure OpenAI evaluates intent and dispatches tool calls:                  │
│     * CalendarTool: create, list, delete events                             │
│     * TaskTool: create, list, toggle status, delete tasks                   │
│     * ReminderTool: set time-based notifications (persistent queue)         │
│     * AlarmTool: set system trigger                                         │
│     * SchedulerTool: configure recurring cron jobs                          │
│ - Spring AI executes `@Tool` methods against PostgreSQL                     │
└──────────────────────────────────────┬──────────────────────────────────────┘
                                       │
                                       ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│ Step 5: Learning & Memory Persistence Layer                                 │
│ - Runs post-turn analysis: identifies new facts/preferences                 │
│ - Deduplicates & updates `user_memories` (pgvector)                         │
└──────────────────────────────────────┬──────────────────────────────────────┘
                                       │
                                       ▼
                         [Stream Response to React UI]
```

---

## 3. Step-by-Step Implementation Roadmap

### Phase 1: Environment & Scaffolding
- [ ] Create `docker-compose.yml` for PostgreSQL 16 with `pgvector/pgvector:pg16`.
- [ ] Initialize `backend/` directory with Maven wrapper (`pom.xml`):
  - Dependencies: `spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `spring-ai-azure-openai-spring-boot-starter`, `postgresql`, `flyway-core` (or JPA ddl-auto).
- [ ] Initialize `frontend/` directory with React + Vite + TypeScript + Tailwind CSS.
- [ ] Configure `backend/src/main/resources/application.yml` for Azure OpenAI configuration (API Key, Endpoint, Deployment Names).

### Phase 2: Relational Schemas & Domain Entities
- [ ] **Chat & User Memory**:
  - `ChatSession` & `ChatMessage` entities.
  - `UserMemory` entity (`id`, `userId`, `factText`, `category`, `embedding` vector).
- [ ] **Document Knowledge Base (RAG)**:
  - `Document` entity (`id`, `filename`, `fileType`, `uploadedAt`).
  - `DocumentChunk` entity (`id`, `documentId`, `chunkIndex`, `content`, `embedding` vector).
- [ ] **Tool Entities**:
  - `CalendarEvent` (`id`, `title`, `description`, `startTime`, `endTime`, `location`).
  - `TaskItem` (`id`, `title`, `description`, `dueDate`, `status`, `priority`).
  - `ReminderItem` (`id`, `message`, `triggerTime`, `isTriggered`, `channel`).
  - `AlarmItem` (`id`, `label`, `triggerTime`, `isActive`, `soundPattern`).
  - `ScheduledJobItem` (`id`, `jobName`, `cronExpression`, `nextRunTime`, `status`).

### Phase 3: The 5 Core Action Tools (Spring AI `@Tool`)
- [ ] **`CalendarTool`**:
  - `@Tool` method: `createCalendarEvent(title, startTime, endTime, location)`
  - `@Tool` method: `listCalendarEvents(startDate, endDate)`
- [ ] **`TaskTool`**:
  - `@Tool` method: `createTask(title, description, dueDate, priority)`
  - `@Tool` method: `listTasks(status)`
  - `@Tool` method: `completeTask(taskId)`
- [ ] **`ReminderTool`**:
  - `@Tool` method: `setReminder(message, triggerTime)`
  - `@Tool` method: `listActiveReminders()`
- [ ] **`AlarmTool`**:
  - `@Tool` method: `setAlarm(label, triggerTime)`
  - `@Tool` method: `dismissAlarm(alarmId)`
- [ ] **`SchedulerTool`**:
  - `@Tool` method: `scheduleJob(jobName, cronExpression)`
- [ ] **Background Dispatcher**:
  - Spring `@Scheduled` service polling pending reminders & alarms every 30 seconds and marking them triggered.

### Phase 4: Hybrid RAG & Query Expansion Pipeline
- [ ] **`QueryExpansionService`**:
  - Azure OpenAI prompt that reformulates user input into 2–3 specific keyword/semantic sub-queries.
- [ ] **`DocumentIngestionService`**:
  - Startup scanner for `./data/documents` (PDF, TXT, Markdown).
  - REST endpoint `POST /api/documents/upload` for file uploads from the UI.
  - Token-based chunking with 10% overlap + Azure OpenAI embedding generation.
- [ ] **`VectorRetrievalService`**:
  - Native `pgvector` repository query using cosine distance (`<=>`).
  - Deduplication and top-k ranking.

### Phase 5: Multi-Tier Memory & Learning Layer
- [ ] **`UserMemoryService`**:
  - Retrieves facts relevant to the active topic from `user_memories`.
- [ ] **`LearningLayerService`**:
  - Post-interaction evaluation: asks the model to output JSON with any newly stated user facts or preferences.
  - Embeds and upserts new facts into `user_memories`.

### Phase 6: Orchestration Engine & REST APIs
- [ ] **`AgentOrchestratorService`**:
  - Coordinates the 5-node pipeline sequentially.
  - Integrates with Spring AI `ChatClient` configured with Azure OpenAI and registered `@Tool` callbacks.
- [ ] **REST Endpoints**:
  - `POST /api/chat`: Chat interaction (supports streaming via Server-Sent Events).
  - `GET /api/tools/dashboard`: Aggregated status of today's calendar, tasks, reminders, and alarms.
  - `GET /api/memories`: View learned memories with manual delete/edit capability.
  - `POST /api/documents/upload`: Document upload for instant RAG indexing.

### Phase 7: React Frontend Application
- [ ] Chat panel with markdown rendering and live tool activity indicators.
- [ ] Side dashboard showing:
  - Upcoming Calendar events.
  - Interactive Todo/Task list.
  - Active Alarms & Reminders.
  - Memory Bank inspector (what RamLaal knows about the user).

### Phase 8: Verification & Demonstration
- [ ] Unit tests for tool schemas, date calculations, and vector operations.
- [ ] End-to-end multi-turn conversation test.
- [ ] Final memory bank synchronization (`.agents/memory/progress.md`).

