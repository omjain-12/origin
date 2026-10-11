# Agent Memory Bank Protocol

This rule defines how AI assistants must interact with and maintain the persistent **Memory Bank** located in `.agents/memory/`.

---

## 1. What is the Memory Bank?

The Memory Bank enables AI assistants to maintain state, context, and institutional knowledge across sessions, turns, and different AI platforms. It bridges the gap between disconnected chat contexts.

The Memory Bank comprises five core documents:
- `projectbrief.md`: High-level vision, core requirements, non-negotiable goals.
- `activeContext.md`: The active focal point. Current sprint/task, recent changes, immediate next steps.
- `systemPatterns.md`: System architecture, structural patterns, and component relationships.
- `techContext.md`: Tech stack choices, dependencies, development environment setup.
- `progress.md`: What works, what is in progress, and the roadmap backlog.

---

## 2. Inbound Workflow (Start of Task)

Whenever starting a new session or tackling a significant feature/bug:
1. **Read `activeContext.md`**: Understand what the current workstream is and what was completed recently.
2. **Read `projectbrief.md`**: Verify that proposed changes align with overarching goals.
3. **Check `techContext.md`**: Ensure proposed code patterns follow existing technology decisions.

---

## 3. Outbound Workflow (Completion of Task)

When finishing a task:
1. **Update `activeContext.md`**:
   - Record what was just changed or implemented.
   - Update the "Immediate Next Steps" list.
2. **Update `progress.md`**:
   - Move completed items from "In Progress" or "Pending" to "Completed".
3. **Update `systemPatterns.md` or `techContext.md`**:
   - If a new architecture pattern, library, or database table was introduced, document it.
4. **Use Skill**:
   - Alternatively, follow the `.agents/skills/memory-sync/SKILL.md` runbook.
