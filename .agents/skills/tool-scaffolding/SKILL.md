---
name: tool-scaffolding
description: >-
  Scaffolds, implements, and registers new tools (such as Calendar, Tasks, Reminders, Alarms,
  or System Schedulers) with strict input validation, typed schemas, and error boundaries.
---

# Tool Scaffolding Skill

Use this skill when adding a new action tool for Origin.

## Tool Requirements Checklist

Every tool created must satisfy:
1. **Name**: Lowercase, descriptive, action-oriented (e.g., `calendar_create_event`, `reminder_schedule`).
2. **Description**: Concise summary explaining to the LLM what the tool does and what input parameters mean.
3. **Strict Input Schema**: DTO / Pydantic class with type annotations and validation (ISO-8601 timestamps, non-empty text, timezone awareness).
4. **Isolated Execution**: Exceptions caught and wrapped in a structured error envelope rather than crashing the process.
5. **Confirmation for Destructive Actions**: Deletion or bulk updates must have an explicit `confirmed: boolean` or user authorization check.

## Scaffolding Procedure

1. **Define Schema**:
   - Create the request input structure and response output structure.
2. **Implement Core Service**:
   - Implement the underlying domain logic (e.g., interacting with the database or calendar API).
3. **Register with Agent Engine**:
   - If Java/Spring AI: Annotate method with `@Tool(description = "...")`.
   - If Python/LangGraph: Define `@tool` function with Pydantic `args_schema`.
4. **Write Unit Tests**:
   - Test successful execution with valid inputs.
   - Test invalid inputs (e.g., end time before start time, invalid cron expression).
   - Test error handling when external services fail.
