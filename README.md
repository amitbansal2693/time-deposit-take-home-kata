# Time Deposit Service — Solution

A refactor of the XA Bank time-deposit kata into a scalable Spring Boot service with a clean
hexagonal architecture. The original assignment is preserved in [`java/Readme.md`](java/Readme.md).

> The Java solution lives in the [`java/`](java) folder. Run all commands from there.

## What it does

Exactly two REST endpoints:

| Method | Path | Purpose |
|---|---|---|
| `GET` | `/api/v1/time-deposits` | List all time deposits with their withdrawals (keyset pagination and field `scope` optional). |
| `PUT` | `/api/v1/time-deposits/balances` | Apply one month of interest to every deposit, in batches. |

Interest per plan: **Basic** 1%, **Student** 3% (none after 1 year), **Premium** 5% (starts after day 45);
no interest in the first 30 days. The shared `TimeDeposit` class and `TimeDepositCalculator.updateBalance`
signature are unchanged.

## Setup

- Java 17+
- Maven 3.9+
- Docker (optional — only for the PostgreSQL profile / Testcontainers test)

## Build and test

```bash
cd java
mvn clean verify
```

## Run

```bash
cd java
mvn spring-boot:run            # in-memory H2 (default "local" profile)
```

PostgreSQL profile:

```bash
cd java
docker compose up -d
mvn spring-boot:run -Dspring-boot.run.profiles=postgres
```

The app starts on <http://localhost:8080>.

- Swagger UI: <http://localhost:8080/swagger-ui.html>
- OpenAPI JSON: <http://localhost:8080/v3/api-docs>

Quick calls:

```bash
B=http://localhost:8080/api/v1/time-deposits
curl "$B"                       # all deposits
curl "$B?scope=basic"           # fewer fields
curl -i -X PUT "$B/balances"    # apply interest
```

## Architecture overview

Hexagonal (ports and adapters):

```
Inbound adapter (REST)  ->  Use-case ports  ->  Application service  ->  Outbound ports  ->  JPA adapters  ->  DB
```

- **Domain**: `TimeDeposit`, `TimeDepositCalculator`, interest policies (`PlanType` enum + `InterestPolicy`).
- **Application**: use-case ports (`application.port.in`), the service (caching, transactions, batching),
  and repository ports (`application.port.out`).
- **Adapters**: web controller/DTOs (`adapter.in.web`) and JPA persistence (`adapter.out.persistence`).
- **Database**: H2 (`local`) or PostgreSQL (`postgres`) — a configuration change only.

See [`ARCHITECTURE.md`](ARCHITECTURE.md) for details.

## Scalability

Batched updates, a single transaction per run, a cross-instance DB lock, keyset pagination, a short-lived
GET cache, a circuit breaker with a last-known-response fallback, and per-instance rate limiting.
See [`SCALABILITY.md`](SCALABILITY.md).

## AI-assisted development

Tools, prompts and how AI output was validated: see [`AI_WORKFLOW.md`](AI_WORKFLOW.md).

## Further documentation (in `java/`)

- [`java/HOW_TO_RUN.md`](java/HOW_TO_RUN.md) — detailed run guide, configuration, request flows.
- [`java/systemDesign.md`](java/systemDesign.md) — full system design.
- [`java/instruction.md`](java/instruction.md) — problem statement and approach.

