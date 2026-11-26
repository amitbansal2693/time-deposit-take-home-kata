package org.ikigaidigital.adapter.out.persistence;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.ikigaidigital.application.port.out.BalanceUpdateRunPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;

import static org.ikigaidigital.common.TimeDepositConstants.BALANCE_UPDATE_LOCK;

/**
 * Uses a row lock instead of an in-JVM lock so the guarantee holds across application instances.
 * The lock row is created by schema.sql (or by a migration when DB_INIT_MODE=never).
 */
@Repository
public class BalanceUpdateRunPersistenceAdapter implements BalanceUpdateRunPort {

    private static final String LOCK_TIMEOUT_HINT = "jakarta.persistence.lock.timeout";

    private final EntityManager entityManager;
    private final long lockTimeoutMillis;

    public BalanceUpdateRunPersistenceAdapter(EntityManager entityManager,
                                              @Value("${time-deposit.update.lock-timeout}") Duration lockTimeout) {
        this.entityManager = entityManager;
        this.lockTimeoutMillis = lockTimeout.toMillis();
    }

    @Override
    public void acquireExclusiveLock() {
        ApplicationLockJpaEntity lock = entityManager.find(ApplicationLockJpaEntity.class, BALANCE_UPDATE_LOCK,
                LockModeType.PESSIMISTIC_WRITE, Map.of(LOCK_TIMEOUT_HINT, lockTimeoutMillis));
        if (lock == null) {
            throw new IllegalStateException("Missing lock row '" + BALANCE_UPDATE_LOCK + "' in applicationLocks");
        }
    }

    @Override
    public boolean isAlreadyApplied(String idempotencyKey) {
        return entityManager.find(BalanceUpdateRunJpaEntity.class, idempotencyKey) != null;
    }

    @Override
    public void recordApplied(String idempotencyKey) {
        entityManager.persist(new BalanceUpdateRunJpaEntity(idempotencyKey, OffsetDateTime.now(ZoneOffset.UTC)));
        entityManager.flush();
    }
}


