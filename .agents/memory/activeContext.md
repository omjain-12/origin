# Active Context: RamLaal

## Current State & Focus
- **Current Phase**: AI Infrastructure & Scaffolding Initialization.
- **Active Task**: Setting up the complete multi-AI agent configuration, memory bank, rules, skills, and MCP structure.
- **Recent Progress**:
  - Initialized master agent guidelines: `AGENTS.md` and `GEMINI.md`.
  - Configured multi-agent IDE compatibility files (`.cursorrules`, `CLAUDE.md`).
  - Created modular `.agents/rules/` for core behaviors, architecture, memory bank protocol, and coding standards.
  - Initialized `.agents/memory/` persistent memory bank documents.
  - Researched and verified top open-source repositories matching the exact 6-node agentic workflow (LangGraph, Jarvis AI Platform, Khoj, Mem0, Letta).
  - Created master `PROJECT_PLAN.md` with verified 8-phase execution plan.
  - Authored comprehensive `IMPLEMENTATION_PROMPT.md` for AI agent handoff and autonomous execution.

## Active Decisions & Considerations
- **Confirmed Tech Stack**:
  - Backend: Java 21, Spring Boot 3.3+, Spring AI 1.0+ (`spring-ai-azure-openai-spring-boot-starter`).
  - Database: PostgreSQL 16 with `pgvector` via `docker-compose.yml`.
  - Tools Execution: Internal database persistence with Spring persistent scheduler.
  - RAG Ingestion: Hybrid (auto-scan `./data/documents` + React UI upload).
  - Build & Monorepo: Maven with `backend/` and `frontend/` (React + Vite + Tailwind CSS).

## Immediate Next Steps
1. Execute Phase 1: Create `docker-compose.yml` for PostgreSQL + `pgvector`.
2. Scaffold `backend/` Spring Boot project with Maven and required Spring AI starters.
3. Configure Azure OpenAI connection in `application.yml`.
4. Scaffold `frontend/` React Vite application.
