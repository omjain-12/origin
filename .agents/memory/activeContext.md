# Active Context: RamLaal

## Current State & Focus
- **Current Phase**: Phase 3 Complete → Ready for Phase 7 (Frontend) after credentials.
- **Active Task**: Awaiting Azure OpenAI credentials from user before running live tool tests.
- **Recent Progress (this session)**:
  - ✅ **Phase 0** — Git init, `.gitignore`, `.env.example`, `docker-compose.yml` (pgvector/pgvector:pg16).
  - ✅ **Phase 1** — Maven `backend/` scaffold with `pom.xml` (Spring Boot 3.3.4, Spring AI 1.0.0 BOM, pgvector, Lombok).
  - ✅ **Phase 2** — All JPA entities + repositories with native pgvector queries.
  - ✅ **Phase 3** — All 5 Spring AI `@Tool` components + BackgroundNotificationDispatcher.
  - ✅ **Phases 4-6** — QueryExpansionService, VectorRetrievalService, DocumentIngestionService, UserMemoryService, LearningLayerService, AgentOrchestratorService.
  - ✅ REST controllers: ChatController, DashboardController, MemoryController, DocumentController.
  - ✅ Initial git commit: `350b0bb` — 70 files, 3135 insertions.

## Active Decisions
- Spring AI 1.0.0 GA used for stable API.
- word-based chunking (configurable via `ramlaal.retrieval.chunk-size/overlap`).
- `ramlaal.memory.user-id=default-user` — single-user mode.

## Immediate Next Steps
1. **User provides Azure OpenAI credentials** → copy `.env.example` to `.env`, fill values.
2. Start `docker-compose up -d` to boot PostgreSQL.
3. Run `mvn spring-boot:run` in `backend/` and verify startup.
4. Phase 7: Scaffold `frontend/` with Vite + React + TypeScript + Tailwind CSS.
