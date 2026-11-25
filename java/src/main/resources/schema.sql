-- Schema per assignment requirements (in-memory H2 for development).
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

