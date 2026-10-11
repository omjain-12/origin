# AGENTS.md - Repository AI Operating Guidelines

Welcome, AI Agent! This file is the primary entry point and operational manual for any AI coding assistant (Google Antigravity, Claude Code, Cursor, Windsurf, Copilot, or CLI agents) working in the **Origin** repository.

---

## 1. Project Mission & Identity

**Origin** is an advanced, autonomous personal AI assistant and workflow orchestrator. Rather than a simple conversational chatbot, it is engineered as a stateful, tool-enabled AI platform featuring:
1. **Persistent Multi-Tier Memory**: Working memory, session history, and durable long-term factual memory.
2. **Hybrid RAG & Retrieval**: Vector search (pgvector), BM25/keyword retrieval, and query expansion.
3. **Tool Execution Engine**: Robust APIs for calendar management, recurring tasks, reminders, alarms, and schedulers.
4. **Learning & Self-Updating Layer**: Selective extraction and persistence of user preferences and facts across interactions.

---

## 2. Core Operational Rules for AI Assistants

When operating in this codebase, you **must adhere** to the following principles:

### A. Memory-First Protocol (Crucial)
1. **Always Inspect Memory Before Acting**: Before starting any task or generating code, read `.agents/memory/activeContext.md` and `.agents/memory/projectbrief.md` to understand the current state and architectural decisions.
2. **Maintain the Memory Bank**: When completing a non-trivial task or establishing an architectural pattern:
   - Update `.agents/memory/activeContext.md` with what changed.
   - Update `.agents/memory/progress.md` with completed features and pending roadmap items.
   - Use the `.agents/skills/memory-sync/SKILL.md` skill workflow.

### B. Architecture & Modularity
- Keep architectural boundaries clean:
  - **API & Session Layer**: Request handling, authentication, conversation tokens.
  - **Context & Retrieval**: Query rewriting, hybrid search, reranking.
  - **Orchestration**: Intent routing, graph/workflow transitions, tool dispatching.
  - **Tool Engine**: Strictly typed schemas, validation, execution, error containment.
  - **Memory Service**: Durable extraction, deduplication, conflict resolution.
- Never write monolithic functions. Prefer small, testable, well-typed modules.

### C. Verification & Quality
- Never declare a feature or fix complete without verifying:
  - Run existing test suites.
  - Verify syntax and linting.
  - Provide evidence of verification in your final response.

---

## 3. Customization Directory Map (`.agents/`)

The `.agents/` directory provides progressive disclosure of rules, skills, and memory:

```
.agents/
├── rules/                  # Hierarchical behavioral and architectural rules
│   ├── 00-agent-core-behavior.md
│   ├── 01-architecture-guidelines.md
│   ├── 02-memory-bank-protocol.md
│   └── 03-coding-standards.md
├── memory/                 # The persistent Memory Bank (survives across chats)
│   ├── projectbrief.md     # Vision, scope, and objectives
│   ├── activeContext.md    # Active focus, recent changes, immediate next steps
│   ├── systemPatterns.md   # Architectural patterns and component topology
│   ├── techContext.md      # Technology stack, versions, setup instructions
│   ├── progress.md         # Implementation roadmap, status, and backlog
│   └── agent.empty         # Empty-state placeholder / directory anchor
├── skills/                 # Progressive runbooks & specialized workflows
│   ├── memory-sync/        # Runbook for updating and validating agent memory
│   ├── tool-scaffolding/   # Standardized pattern for adding new tools
│   ├── rag-and-retrieval/  # Guidelines for indexing, retrieval, and reranking
│   └── code-verification/  # Test execution and verification checklist
├── workflows/              # Reusable agent task recipes
│   └── agent.empty         # Workflow directory anchor
├── mcp_config.json         # Model Context Protocol integrations
└── hooks.json              # Agent lifecycle hooks (pre/post tool execution)
```

---

## 4. Multi-Tool Compatibility

- **Google Antigravity (`agy`)**: Reads `GEMINI.md`, `AGENTS.md`, and natively mounts `.agents/` (rules, skills, hooks, and MCP).
- **Cursor IDE**: Reads `.cursorrules` (which references this file and `.agents/rules/`).
- **Claude Code**: Reads `CLAUDE.md` (which inherits these standards).
- **Windsurf / Copilot**: Respects `AGENTS.md` and `.agents/rules/`.
