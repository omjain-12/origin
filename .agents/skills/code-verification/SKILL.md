---
name: code-verification
description: >-
  Executes comprehensive code verification, test runs, static analysis, and regression checks
  before marking tasks as complete.
---

# Code Verification Skill

Use this skill before reporting task completion to verify code correctness and avoid regressions.

## Verification Checklist

1. **Syntax & Compilation**:
   - For Java: Run `./mvnw clean test-compile` or `./gradlew testClasses`.
   - For Python: Run `python -m py_compile <files>` or `mypy .`.

2. **Automated Tests**:
   - Run existing unit and integration tests.
   - Verify that test assertions pass with zero failures.

3. **Lint & Formatting**:
   - Check formatting (Spotless / Checkstyle for Java, Ruff / Black for Python).

4. **Sanity Check Diffs**:
   - Review file diffs to ensure no unintended files or temporary debug prints were committed.

5. **Report to User**:
   - Summarize the test commands executed and their output results.
