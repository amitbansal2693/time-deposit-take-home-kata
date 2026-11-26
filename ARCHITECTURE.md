# Architecture

## Style: Hexagonal (ports and adapters)

The domain and application logic sit in the centre and depend only on interfaces (ports). Everything
technology-specific (HTTP, JPA, the database) is an adapter plugged into those ports. This keeps the
business rules isolated and lets the database or web layer change without touching the core.

```
          Inbound adapter                 Application core                     Outbound adapters
  ┌──────────────────────────┐   ┌──────────────────────────────┐   ┌──────────────────────────────┐
  │ TimeDepositController     │──▶│ GetAllTimeDepositsUseCase     │   │ TimeDepositRepositoryPort     │
  │ DTOs, filters, handlers   │   │ UpdateAll...BalancesUseCase   │   │   -> TimeDepositPersistence…  │
  │ (adapter.in.web)          │   │  (application.port.in)        │   │ BalanceUpdateRunPort          │
  └──────────────────────────┘   │                               │──▶│   -> BalanceUpdateRun…        │
                                  │ TimeDepositApplicationService │   │ (application.port.out +       │
                                  │ (application.service)         │   │  adapter.out.persistence)     │
                                  └───────────────┬──────────────┘   └───────────────┬──────────────┘
                                                  │ domain                            │
                                  ┌───────────────▼──────────────┐                    ▼
                                  │ TimeDeposit, Calculator,      │              H2 / PostgreSQL
                                  │ PlanType, InterestPolicy      │
                                  └──────────────────────────────┘
```

## Packages

| Package | Responsibility |
|---|---|
| `org.ikigaidigital` | Shared domain: `TimeDeposit`, `TimeDepositCalculator`. |
| `interest` | `PlanType` enum, `InterestPolicy` + per-plan policies, `InterestPolicyRegistry`. |
| `common` | `TimeDepositConstants` — one source of truth for rules, paths and tuning. |
| `application.port.in` | Inbound use-case interfaces and read models (`TimeDepositView`). |
| `application.port.out` | Outbound repository/lock interfaces. |
| `application.service` | `TimeDepositApplicationService` — caching, transactions, batching. |
| `adapter.in.web` | REST controller, DTOs, filters, exception handler, config properties. |
| `adapter.out.persistence` | JPA entities, repositories and adapters. |
| `config` | Caching, web (ETag filter) and OpenAPI configuration. |

## Design patterns and decisions

- **Strategy (interest)** — each plan's rule is an `InterestPolicy`; `PlanType` maps a code to its policy.
  Adding a plan is one enum constant + one policy class. Unknown/mismatched codes earn no interest,
  matching the original calculator exactly.
- **Ports and adapters** — the service depends on `TimeDepositRepositoryPort`, not on JPA, so the database
  is swappable (H2 ↔ PostgreSQL) by profile only.
- **DTO separation** — `TimeDepositResponse`/`WithdrawalResponse` are separate from JPA entities and from
  the shared `TimeDeposit`, so the API contract can evolve independently.
- **XML-defined response scopes** — `?scope=basic|default|full` selects which fields are returned; scopes are
  declared in `dto/time-deposit-response-scopes.xml` and exposed through the `Scope` enum.
- **Constructor injection** — immutable, fail-fast, test-friendly; no field injection.
- **Problem Details (RFC 7807)** — errors returned as standard problem JSON via `ApiExceptionHandler`.

## Key constraints honoured

- `TimeDeposit` and `TimeDepositCalculator.updateBalance(List<TimeDeposit>)` are unchanged.
- Exactly two endpoints.
- Original rounding behaviour preserved (`double` calculation + `BigDecimal` HALF_UP); balances are stored
  as `DECIMAL(19,2)`. This trade-off is documented rather than claiming fully precise arithmetic.

## Testing

- Unit: calculator, each interest policy (boundary + nearby days, parameterized), `PlanType`, registry,
  service (Mockito), config and response scopes.
- Integration: the shared `TimeDepositApiContract` runs against H2 (`TimeDepositControllerIT`) and a real
  PostgreSQL container (`TimeDepositControllerPostgresIT`, Testcontainers); resilience behaviour in
  `TimeDepositResilienceIT`.

