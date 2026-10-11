---
name: memory-sync
description: >-
  Synchronizes and updates the project Memory Bank (.agents/memory/) when tasks are completed,
  new patterns are introduced, or architectural decisions are finalized.
---

# Memory Sync Skill

Use this skill whenever concluding a meaningful development task, altering system design, or ending a work session.

## Steps

1. **Review Session Changes**:
   - Inspect files modified during the turn.
   - Identify which features, bug fixes, or schema alterations occurred.

2. **Update `activeContext.md`**:
   - Navigate to [activeContext.md](../../memory/activeContext.md).
   - Under `Recent Progress`, append a bullet summarizing the changes.
   - Under `Immediate Next Steps`, remove completed items and add newly identified follow-up tasks.

3. **Update `progress.md`**:
   - Navigate to [progress.md](../../memory/progress.md).
   - Check off completed checkboxes `[x]` under the active phase.
   - If an item was blocked or newly discovered, add it to the roadmap backlog.

4. **Update `systemPatterns.md` or `techContext.md` (if applicable)**:
   - If a new library was added or an architectural boundary was modified, update [systemPatterns.md](../../memory/systemPatterns.md) or [techContext.md](../../memory/techContext.md).

5. **Confirm with User**:
   - Provide a brief 1-line note to the user confirming that the Memory Bank has been kept up-to-date.
