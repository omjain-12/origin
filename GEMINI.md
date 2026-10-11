# GEMINI.md - Google Antigravity Native Workspace Configuration

This file provides root configuration and instructions specifically tailored for **Google Antigravity** (`agy`), including Antigravity 2.0, Antigravity IDE, and the Antigravity agent execution loop.

---

## 1. Antigravity Agent Directives

1. **Customization Discovery**:
   - Project-level customizations are stored in `.agents/`.
   - Rules in `.agents/rules/` are loaded hierarchically.
   - Project skills reside in `.agents/skills/<skill-name>/SKILL.md`. Check available workspace skills before executing complex workflows.
   - MCP configurations are specified in `.agents/mcp_config.json`.
   - Lifecycle hooks are defined in `.agents/hooks.json`.

2. **Memory Bank Adherence**:
   - At the beginning of non-trivial coding tasks, inspect `.agents/memory/activeContext.md` and `.agents/memory/projectbrief.md`.
   - Maintain the memory bank whenever completing a task, making architectural decisions, or updating dependencies.

3. **Subagent Delegation Policy**:
   - Use `invoke_subagent` for parallel research, isolated refactoring, or independent tasks.
   - Delegate large codebase surveys to the `research` subagent to preserve context window.

4. **Safety & Tool Execution**:
   - Always run verification and tests before concluding a turn.
   - Follow the guidelines in `.agents/rules/00-agent-core-behavior.md`.
   - Maintain documentation integrity: preserve docstrings and existing architectural comments.

---

## 2. Quick Skill Directory

- **Memory Sync**: `.agents/skills/memory-sync/SKILL.md` (Update project memory files)
- **Tool Scaffolding**: `.agents/skills/tool-scaffolding/SKILL.md` (Add new assistant tools)
- **RAG & Retrieval**: `.agents/skills/rag-and-retrieval/SKILL.md` (Vector & hybrid search setup)
- **Code Verification**: `.agents/skills/code-verification/SKILL.md` (Quality check and test execution)
