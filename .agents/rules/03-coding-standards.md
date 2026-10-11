# Coding Standards & Quality Guidelines

This rule establishes software quality, clean code, and testing standards for all code generated in this repository.

---

## 1. Code Quality & Modularity
- **Strict Typing**: All code must include explicit type annotations (Java types, TypeScript interfaces, Python type hints with Pydantic/dataclasses).
- **Single Responsibility**: Classes, modules, and functions should do one thing with clear inputs and outputs.
- **Fail Fast & Explicit Errors**: Throw specific, typed domain exceptions rather than catching generic errors or silently returning `null`/`None`.
- **Environment & Secrets**: Never hardcode API keys, database credentials, or sensitive URLs. Always read from environment variables or secure configuration managers.

---

## 2. API & Tool Contracts
- All AI tools (calendar, reminders, alarms, search) must have:
  - Input schema validation (e.g., ISO-8601 date strings, time zone validation, non-empty text).
  - Explicit return structures including `status`, `data`, and actionable error messages when execution fails.
  - Idempotency where applicable.

---

## 3. Testing & Verification
- Unit test individual tool execution logic and query expansion transformations.
- Integration test vector search retrieval and database persistence.
- Do not mark tasks complete without running verification checks or test suites.
