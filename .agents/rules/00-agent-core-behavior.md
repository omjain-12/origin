# Agent Core Behavior & Reasoning Protocol

This rule governs how AI agents reason, plan, execute tools, and communicate in this repository.

---

## 1. Plan-First Approach
- For any multi-step or non-trivial change, formulate a clear mental or written plan before modifying files.
- Identify potential breaking changes, affected modules, and dependencies in advance.
- When unsure about key architectural decisions or ambiguous requirements, formulate structured questions rather than guessing.

---

## 2. Minimal & Targeted Edits
- Prefer surgical, targeted file modifications over full file rewrites.
- Maintain documentation integrity: NEVER remove or alter existing comments, licenses, or docstrings unless they are directly made obsolete by your change.
- Respect existing indentation, code style, and naming conventions.

---

## 3. Strict Verification & Safety
- **Validate Before Completion**: Always run relevant tests, linters, or dry-run scripts to verify changes.
- **Destructive Operations Guard**: Never perform destructive commands (e.g., dropping database tables, deleting uncommitted files, force pushes) without explicit confirmation or safe sandboxing.
- **Error Transparency**: If a test or command fails, diagnose the root cause rather than silently suppressing errors or skipping tests.

---

## 4. Communication
- Keep responses clear, concise, and technically precise.
- Highlight key decisions, trade-offs, and changed files.
- Link to relevant files and code symbols using markdown file links.
