# Active Context: Origin

## Current State & Focus
- **Current Phase**: Phase 7 Complete (Frontend Built & Verified) → Phase 8 (Runtime Verification).
- **Active Task**: All frontend views, theme system, and backend scaffolding complete. Ready for runtime database boot when Docker Desktop is active.
- **Recent Progress**:
  - ✅ **Complete Rename**: Entire repository, packages (`com.origin.*`), classes, configs, and root directory renamed from RamLaal to **Origin**. Zero traces remain.
  - ✅ **LLM Provider**: AWS Bedrock Mantle OpenAI-compatible endpoint with `zai.glm-5` (LLM) and `zai.glm-4.7` (embeddings) tested live.
  - ✅ **Phase 7 (React Frontend)**:
    - Editorial SaaS aesthetic inspired by reference design (warm ivory, periwinkle, lavender, sage).
    - Dual **Light & Dark Mode** toggle with persistent `localStorage` preference.
    - **Assistant Chat View**: Welcome hero, prompt suggestion chips, message thread, keyboard shortcut support.
    - **Productivity Dashboard View**: KPI cards (tasks, events, reminders, alarms), timeline schedule, active todos with priority badges, cron jobs.
    - **Durable Memory Bank View**: Stored user facts categorized and filterable with instant deletion.
    - **Document Knowledge Base (RAG) View**: File dropzone for `.txt`/`.md` with chunking/vector status.
    - Full TypeScript type-safety verified (`tsc -b && vite build` passed with zero errors).

## Active Decisions
- Frontend communicates with Spring Boot backend via REST endpoints + Vite proxy (`/api` -> `http://localhost:8080`).
- Model parameters simplified to two environment variables: `LLM_MODEL` and `EMBEDDING_MODEL`.
- Theme system managed via CSS custom properties and `data-theme` attribute on root document.

## Immediate Next Steps
1. Boot Docker Desktop and start PostgreSQL: `docker compose up -d`.
2. Start backend: `mvn spring-boot:run` (or run via IDE).
3. Start frontend: `npm run dev` in `frontend/`.
4. Execute end-to-end multi-turn verification prompt.
