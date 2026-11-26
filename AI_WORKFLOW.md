# AI-Assisted Workflow

The solution was designed and driven by me; an AI coding assistant was used as a targeted helper for
boilerplate, first drafts and test scaffolding. Every design decision, trade-off and final version was
made and reviewed by me. This document records the tools, how they were used, and how AI output was checked.

## Tools

| Tool | Role |
|---|---|
| GitHub Copilot (agent mode) | Suggestions, boilerplate, draft tests and docs — always reviewed before keeping. |
| JetBrains IntelliJ IDEA | Primary development, debugging, Maven build, running tests. |
| Maven + JUnit 5 / Mockito / Testcontainers | My validation harness for anything AI drafted. |

## How I worked

- I decided the architecture (hexagonal), the scalability approach, the interest-policy design, the API
  contract, the configuration model and the test strategy.
- I used the assistant in short loops for mechanical work: describe a small goal → let it draft → **I review
  the diff, correct it, and keep only what fits** → run `mvn clean verify`.
- I treated the original `Readme.md` as the fixed spec and never changed it, and I kept the shared
  `TimeDeposit` / `TimeDepositCalculator.updateBalance` signature fixed as the assignment requires.

## Guardrails I set for the assistant

- Keep exactly two endpoints; don't add more.
- Don't change the shared `TimeDeposit` class or the `updateBalance(List<TimeDeposit>)` signature.
- Preserve the original interest results and rounding (I verify this with tests).
- Hexagonal layering, constants over magic values, configuration over hard-coded values.
- Every change must keep `mvn clean verify` green.
- Interest tests must cover each boundary and the days around it.

## Split of work

| Area | Who led | How AI helped |
|---|---|---|
| Architecture, package layout, design decisions | Me | Occasional suggestions; I chose the structure. |
| Interest rules and correctness (boundaries, rounding) | Me | Drafted policy classes; I verified values against the kata. |
| Scalability design (batching, lock, idempotency, paging, cache, resilience) | Me | Drafted well-known patterns; I decided what to include and tuned it. |
| Tests (unit, Mockito, parameterized boundaries, Testcontainers) | Me | Generated first drafts; I fixed gaps and added boundary cases. |
| Boilerplate (DTOs, mappers, config wiring) | AI drafted | I reviewed and adjusted. |
| Documentation | AI drafted | I edited for accuracy and trimmed. |

## How I validated AI output

- `mvn clean verify` after every change — all unit and integration tests must pass.
- Interest results asserted against the original kata values (e.g. `1235595.81`) so refactoring can't change
  behaviour.
- Boundary tests at the exact day thresholds and the days immediately around them for each plan.
- The same API contract runs on H2 and on a real PostgreSQL container to confirm portability.
- I read every diff; where AI used magic values or hard-coded config, I moved them into constants/properties.

## Reproducibility

1. Open the `java/` project in an IDE with an AI assistant.
2. Provide the guardrails above as the task instructions.
3. Work goal-by-goal, running `mvn clean verify` each time and reviewing each diff before accepting.

