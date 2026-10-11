# Tech Context: Origin

## Confirmed Technology Stack

### Backend
- **Runtime**: Java 21+ LTS
- **Framework**: Spring Boot 3.3+, Spring AI 1.0+ (`spring-ai-azure-openai-spring-boot-starter`)
- **LLM Provider**: Azure OpenAI (GPT-4o / GPT-4o-mini / text-embedding-3)
- **Tool Mechanism**: Spring AI `@Tool` / function calling
- **Workflow Orchestration**: Spring AI Advisors & Custom Stateful Pipeline (`Memory` -> `Query Expansion` -> `Retrieval` -> `Tool Calling` -> `Learning Layer`)
- **Database**: PostgreSQL 16+ with `pgvector`
- **Scheduler**: JobRunr / Spring `@Scheduled` / Quartz for persistent reminders, alarms & tasks

### Frontend
- **Framework**: React 18+ (Vite, TypeScript, Tailwind CSS)
- **Features**: Chat window, tool activity feed, calendar/task viewer, memory inspector

## Environment & Tooling
- **OS**: Windows (PowerShell)
- **AI Assist Platforms**: Google Antigravity (AGY), Cursor, Claude Code
- **Version Control**: Git
- **Containerization**: Docker Compose (for PostgreSQL + pgvector and Ollama/local models)
