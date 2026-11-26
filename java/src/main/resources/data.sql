-- Demo seed data. ON CONFLICT DO NOTHING makes it safe to re-run on a persistent database.
INSERT INTO "timeDeposits" ("id", "planType", "days", "balance") VALUES
    (1, 'basic', 45, 1234567.00),
    (2, 'student', 100, 5000.00),
    (3, 'premium', 10, 20000.00),
    (4, 'premium', 90, 15000.00),
    (5, 'student', 400, 3000.00),
    (6, 'basic', 10, 1000.00)
ON CONFLICT DO NOTHING;

INSERT INTO "withdrawals" ("id", "timeDepositId", "amount", "date") VALUES
    (1, 1, 500.00, '2026-01-10'),
    (2, 2, 100.00, '2026-02-15'),
    (3, 4, 250.00, '2026-03-01')
ON CONFLICT DO NOTHING;

