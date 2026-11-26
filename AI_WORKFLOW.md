# AI-Assisted Workflow

This exercise was completed with an AI coding assistant used as a pair-programmer. This document records
the tools, how they were configured, which parts were AI-assisted, and how the output was validated.

## Tools

| Tool | Role |
|---|---|
| GitHub Copilot (agent mode) | Code generation, refactoring, test writing, documentation. |
| JetBrains IntelliJ IDEA | IDE, Maven build, running tests. |
| Maven + JUnit 5 / Mockito / Testcontainers | Build and automated validation of AI-produced code. |

## How it was set up

- Worked in the IDE with the assistant in an agentic loop: describe a goal → the agent edits files and runs
  `mvn clean verify` → review the diff and test results → iterate.
- The repository `Readme.md` (the original assignment) was treated as the fixed source of truth and never
  modified; the assistant was explicitly told which files must not change
  (`TimeDeposit`, `TimeDepositCalculator.updateBalance`).

## Guardrails / instructions given to the assistant

- Keep exactly two endpoints; do not add more.
- Do not change the shared `TimeDeposit` class or the `updateBalance(List<TimeDeposit>)` signature.
- Preserve the original interest results and rounding (verified by tests).
- Prefer hexagonal architecture, constants over magic values, and configuration over hard-coded values.
- Every change must keep `mvn clean verify` green.
- Tests must cover all interest boundaries and nearby dates; name tests `testMethod_whenCondition_thenResult`.

## What was AI-assisted (and why)

| Area | AI-assisted | Reason |
|---|---|---|
| Hexagonal scaffolding (ports, adapters, service) | Yes | Repetitive boilerplate; fast to generate and review. |
| Interest policies + `PlanType` enum | Yes | Clear pattern; AI applied it consistently. |
| Scalability features (batching, keyset paging, lock, idempotency, cache, circuit breaker, rate limit) | Yes | Well-known patterns the assistant could assemble quickly. |
| Tests (unit, Mockito, parameterized boundaries, Testcontainers) | Yes | Broad coverage generated fast, then reviewed. |
| Documentation (this file, README, ARCHITECTURE, SCALABILITY, HOW_TO_RUN) | Yes | Drafted by AI, edited for accuracy. |
| Business rules and final design decisions | Human-reviewed | Correctness and trade-offs confirmed by a person. |

## How AI output was validated

- `mvn clean verify` after every change — unit and integration tests must pass.
- Interest results asserted against the original kata values (e.g. `1235595.81`) so refactoring cannot change
  behaviour.
- Boundary tests at the exact day thresholds and the days immediately around them for each plan.
- Integration tests run the same API contract on both H2 and a real PostgreSQL container, confirming
  portability.
- Manual review of every diff; magic values and hard-coded config were pulled into constants/properties.

## Reproducibility

1. Open the `java/` project in an IDE with an AI coding assistant.
2. Provide the guardrails above as the system/task instructions.
3. Drive changes goal-by-goal, running `mvn clean verify` each time and reviewing the diff before accepting.

