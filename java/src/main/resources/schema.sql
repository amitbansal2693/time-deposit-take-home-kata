-- Schema per assignment requirements. Portable across H2 (dev) and PostgreSQL.
CREATE TABLE IF NOT EXISTS "timeDeposits" (
    "id" INTEGER PRIMARY KEY,
    "planType" VARCHAR(50) NOT NULL,
    "days" INTEGER NOT NULL,
    "balance" DECIMAL(19, 2) NOT NULL
);

CREATE TABLE IF NOT EXISTS "withdrawals" (
    "id" INTEGER PRIMARY KEY,
    "timeDepositId" INTEGER NOT NULL,
    "amount" DECIMAL(19, 2) NOT NULL,
    "date" DATE NOT NULL,
    CONSTRAINT fk_withdrawals_time_deposit FOREIGN KEY ("timeDepositId") REFERENCES "timeDeposits" ("id")
);

-- Supports the withdrawals join used by GET /api/time-deposits.
CREATE INDEX IF NOT EXISTS idx_withdrawals_time_deposit_id ON "withdrawals" ("timeDepositId");

-- Supporting tables (not part of the required API schema).

-- One row per named lock; PUT takes SELECT ... FOR UPDATE on it so only one balance update
-- runs at a time, even with several application instances.
CREATE TABLE IF NOT EXISTS "applicationLocks" (
    "name" VARCHAR(50) PRIMARY KEY
);
INSERT INTO "applicationLocks" ("name") VALUES ('balance-update') ON CONFLICT DO NOTHING;

-- Idempotency keys of balance updates already applied; a retried PUT with the same key is a no-op.
CREATE TABLE IF NOT EXISTS "balanceUpdateRuns" (
    "idempotencyKey" VARCHAR(100) PRIMARY KEY,
    "executedAt" TIMESTAMP WITH TIME ZONE NOT NULL
);

