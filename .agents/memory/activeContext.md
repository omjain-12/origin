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
  - ✅ **Interactive Right Sidebar (Glance & Schedule)**:
    - Scoped specifically to the **Chat View** with persistent collapse/expand state (`origin_right_sidebar_open`).
    - Collapsible into a **50px slim icon rail** with badge indicators (⚡, 🔔, ⏰, ✅, 📅) or expandable to **320px**.
    - **Spotlight Hero Card**: Dynamically pinpoints the nearest upcoming event/alarm/reminder with relative countdown ("in 12m", "Just now").
    - **Accordion Dropdowns**: Discrete expandable drawers for Reminders, Alarms, Tasks, and Calendar Events.
    - **Quick Filter Pills**: `All`, `Today`, `Urgent`.
    - **Optional Direct Actions**: Added PATCH endpoints to `DashboardController` (`/reminders/{id}/dismiss`, `/alarms/{id}/dismiss`, `/tasks/{id}/toggle`) with optimistic frontend UI updates.
    - ✅ **Temporal & Date-Alignment Fix**:
      - Fixed Right Sidebar filter behavior: Spotlight hero card now strictly filters by active tab (`today`, `urgent`, `all`). When `today` is selected and has no items, renders an encouraging "All Clear for Today" banner instead of showing future items.
      - Fixed Backend temporal awareness: Injected dynamic `{current_time}` and `{day_of_week}` into `AgentOrchestratorService` `SYSTEM_PROMPT` so LLM accurately calculates relative dates ("in 30 mins", "today", "tomorrow").
      - Removed hardcoded `2026-10-15` examples in tool schemas (`ReminderTool`, `AlarmTool`, `CalendarTool`), and added optional `dueDate` support to `TaskTool`.
    - ✅ **Shipping Readiness & User Abstraction (High Priority)**:
      - Stripped raw technical identifiers (Session IDs, Task UUIDs, Alarm/Reminder/Calendar/Job UUIDs) from all frontend views and backend tool returns.
      - Upgraded tool mutations (`completeTask`, `deleteTask`, `dismissAlarm`, `cancelReminder`, `deleteCalendarEvent`) with natural language title/message fallback matching, allowing users to modify items by name without UUIDs.
      - Added strict ID suppression and conversational confirmation rules to `AgentOrchestratorService` `SYSTEM_PROMPT`.
      - Sanitized `ChatController` error responses to prevent leaking Java exception stack traces to users.
      - Cleaned up developer jargon (e.g., `pgvector`, `(RAG)`, `zai.glm-5 / Bedrock Mantle`) in `Sidebar` and `DocumentsView`.
    - ✅ **Repository Handover & Gitignore Hygiene**:
      - Untracked dev prompts, test files, and scratch artifacts (`refrence_chatgpt_response.txt`, `IMPLEMENTATION_PROMPT.md`, `PROJECT_PLAN.md`, `TEST_PROMPTS.md`, `test_*.py`, root `mvn.cmd`/`mvn.ps1`, `backend/src/test/`) so another developer receives clean, production-grade source code.
      - Added comprehensive ignoring rules in `.gitignore` preserving local development files while keeping git commits minimal and focused.
    - Verified with `mvn test-compile` (Backend) and `npm run build` (Frontend).

## Active Decisions
- Right sidebar sits neatly inside `ChatView`, leaving full screen for other views (Dashboard, Memory, Documents).
- Polling interval set to 15s with instant trigger on `handleSend` / chat tool completions.

## Immediate Next Steps
1. Boot Docker Desktop and start PostgreSQL: `docker compose up -d`.
2. Start backend: `mvn spring-boot:run` (or run via IDE).
3. Start frontend: `npm run dev` in `frontend/`.
4. Execute end-to-end multi-turn verification prompt.

