package org.ikigaidigital.common;

/**
 * Single source of truth for business rules, API paths and tuning values.
 */
public final class TimeDepositConstants {

    private TimeDepositConstants() {
    }

    // Plan types (matched exactly, as in the original calculator)
    public static final String PLAN_BASIC = "basic";
    public static final String PLAN_STUDENT = "student";
    public static final String PLAN_PREMIUM = "premium";

    // Annual interest rates
    public static final double BASIC_ANNUAL_RATE = 0.01;
    public static final double STUDENT_ANNUAL_RATE = 0.03;
    public static final double PREMIUM_ANNUAL_RATE = 0.05;
    public static final int MONTHS_PER_YEAR = 12;

    // Day thresholds: interest applies only when days > *_INTEREST_FREE_DAYS
    public static final int INTEREST_FREE_DAYS = 30;
    public static final int PREMIUM_INTEREST_FREE_DAYS = 45;
    /** Student plans earn no interest from this day onwards (after one year). */
    public static final int STUDENT_MAX_DAYS_EXCLUSIVE = 366;

    public static final int MONEY_SCALE = 2;

    // REST API (versioned so the contract can evolve without breaking clients)
    public static final String API_BASE_PATH = "/api/v1/time-deposits";
    public static final String BALANCES_PATH = "/balances";

    // HTTP headers
    public static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";
    public static final String IDEMPOTENCY_REPLAYED_HEADER = "Idempotency-Replayed";
    public static final String NEXT_AFTER_ID_HEADER = "X-Next-After-Id";
    public static final String DATA_STALE_HEADER = "X-Data-Stale";
    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    public static final String CORRELATION_ID_MDC_KEY = "correlationId";
    /** Size of "balanceUpdateRuns"."idempotencyKey" in schema.sql; the configured maximum may not exceed it. */
    public static final int IDEMPOTENCY_KEY_COLUMN_LENGTH = 100;

    // Caches
    public static final String TIME_DEPOSITS_CACHE = "timeDeposits";
    public static final String LAST_KNOWN_RESPONSE_CACHE = "timeDepositsLastKnown";

    // Resilience4j instance names. Each name is the key of its settings in application.properties
    // (overridable per profile), e.g. READ_RATE_LIMITER -> resilience4j.ratelimiter.instances.timeDepositsRead.*
    public static final String READ_RATE_LIMITER = "timeDepositsRead";
    public static final String UPDATE_RATE_LIMITER = "timeDepositsUpdate";
    public static final String READ_CIRCUIT_BREAKER = "timeDepositsRead";

    // Batch processing
    public static final String UPDATE_BATCH_SIZE_PROPERTY = "time-deposit.update-batch-size";

    /** Row in "applicationLocks" that serialises balance updates across all application instances. */
    public static final String BALANCE_UPDATE_LOCK = "balance-update";
}

