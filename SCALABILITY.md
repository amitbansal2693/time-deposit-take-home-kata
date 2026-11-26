# Scalability

The guiding idea: handle a large `timeDeposits` table and concurrent traffic with bounded memory and
predictable behaviour, without adding heavyweight infrastructure (no Kafka/Redis/microservices).

## Batch processing (PUT)

- Deposits are read and written in fixed-size batches (`UPDATE_BATCH_SIZE`, default 500) using **keyset
  iteration** (`WHERE id > :lastId ORDER BY id LIMIT :size`), so memory stays constant regardless of table size.
- Writes use Hibernate JDBC batching (`hibernate.jdbc.batch_size=100`); each batch is flushed and the
  persistence context cleared, so the heap does not grow with the number of rows.

## Transactions

- The whole monthly update runs in **one transaction** — it is all-or-nothing.
- A global upper bound (`TX_TIMEOUT`, default 300s) stops a runaway update from holding resources forever.
- Reads run in `@Transactional(readOnly = true)`.

## Concurrency control

- A row in `applicationLocks` is taken with `SELECT ... FOR UPDATE` (pessimistic write) at the start of the
  update, so **only one update runs at a time across all instances** (a JVM lock would not be enough).
- A waiting request gives up after `UPDATE_LOCK_TIMEOUT` (default 10s) and returns `409 Conflict`.

## Idempotency

- An optional `Idempotency-Key` is recorded in `balanceUpdateRuns` inside the same transaction.
- The key is checked **after** acquiring the lock, so a retry that was queued behind the first run sees the
  first run's committed key and safely returns without applying interest twice (`Idempotency-Replayed: true`).

## Pagination (GET)

- **Keyset (cursor) pagination** via `afterId` + `limit`: ids are selected first, then loaded with their
  withdrawals. Unlike offset pagination it stays fast at any depth. `X-Next-After-Id` points to the next page.
- Results are deterministically ordered by `id`.

## Caching

- A short-lived Caffeine cache (`CACHE_TTL`, default 30s) serves repeated GETs without hitting the database;
  it is evicted on every successful update (cache advice is ordered outside the transaction).
- HTTP `ETag`/`If-None-Match` returns `304 Not Modified` for unchanged responses, saving bandwidth.

## Resilience

- **Rate limiting** (Resilience4j, per instance): GET 50/s, PUT 5/min by default → `429` when exceeded.
- **Circuit breaker** on GET: when the database is failing, the last successful response is returned, marked
  `X-Data-Stale: true` (its own short TTL); if nothing is cached, `503`.

## Trade-offs

- **Per-instance** caches and rate limiters. Global limits belong at an API gateway; a shared cache (Redis)
  can be added behind the same ports if needed.
- **Monetary precision**: the shared calculator uses `double` to preserve the original rounding; storage is
  `DECIMAL(19,2)`. Documented rather than claiming end-to-end exact arithmetic.
- **Single-node lock** via the database row is simple and correct across instances; a dedicated scheduler or
  distributed lock service could replace it if the update moves to a background job.

## Scaling further (if required)

- Move the PUT to a scheduled/background job with progress tracking.
- Introduce Redis for a shared cache and distributed rate limiting.
- Add read replicas for GET, keeping writes on the primary.

All of these fit behind the existing ports without changing the domain.

