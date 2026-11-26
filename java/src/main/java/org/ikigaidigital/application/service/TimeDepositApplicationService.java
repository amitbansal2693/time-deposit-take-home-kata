package org.ikigaidigital.application.service;

import org.ikigaidigital.TimeDeposit;
import org.ikigaidigital.TimeDepositCalculator;
import org.ikigaidigital.application.port.in.BalanceUpdateResult;
import org.ikigaidigital.application.port.in.GetAllTimeDepositsUseCase;
import org.ikigaidigital.application.port.in.TimeDepositView;
import org.ikigaidigital.application.port.in.UpdateAllTimeDepositBalancesUseCase;
import org.ikigaidigital.application.port.out.BalanceUpdateRunPort;
import org.ikigaidigital.application.port.out.TimeDepositRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.ikigaidigital.common.TimeDepositConstants.TIME_DEPOSITS_CACHE;
import static org.ikigaidigital.common.TimeDepositConstants.UPDATE_BATCH_SIZE_PROPERTY;

/**
 * Cache advice is ordered outside the transaction (see CacheConfig): a cache hit opens no
 * transaction, and the eviction after an update happens only once the update has committed.
 */
@Service
public class TimeDepositApplicationService implements GetAllTimeDepositsUseCase, UpdateAllTimeDepositBalancesUseCase {

    private static final Logger log = LoggerFactory.getLogger(TimeDepositApplicationService.class);

    private final TimeDepositRepositoryPort repository;
    private final BalanceUpdateRunPort updateRuns;
    private final TimeDepositCalculator calculator;
    private final int batchSize;

    public TimeDepositApplicationService(TimeDepositRepositoryPort repository,
                                         BalanceUpdateRunPort updateRuns,
                                         TimeDepositCalculator calculator,
                                         @Value("${" + UPDATE_BATCH_SIZE_PROPERTY + "}") int batchSize) {
        this.repository = repository;
        this.updateRuns = updateRuns;
        this.calculator = calculator;
        this.batchSize = batchSize;
    }

    @Override
    @Cacheable(cacheNames = TIME_DEPOSITS_CACHE, key = "'all'")
    @Transactional(readOnly = true)
    public List<TimeDepositView> getAllTimeDeposits() {
        return repository.findAllWithWithdrawals();
    }

    @Override
    @Cacheable(cacheNames = TIME_DEPOSITS_CACHE, key = "#afterId + ':' + #limit")
    @Transactional(readOnly = true)
    public List<TimeDepositView> getTimeDeposits(int afterId, int limit) {
        return repository.findPageWithWithdrawals(afterId, limit);
    }

    /**
     * Processes deposits in fixed-size batches so memory stays constant regardless of table size.
     * A single transaction keeps the monthly update all-or-nothing; the lock stops two runs
     * (on any instance) from applying interest at the same time.
     */
    @Override
    @CacheEvict(cacheNames = TIME_DEPOSITS_CACHE, allEntries = true)
    @Transactional
    public BalanceUpdateResult updateAllBalances(String idempotencyKey) {
        updateRuns.acquireExclusiveLock();
        // Checked after taking the lock, so a retry waiting on the lock sees the first run's key.
        if (idempotencyKey != null && updateRuns.isAlreadyApplied(idempotencyKey)) {
            log.info("Balance update skipped: idempotency key '{}' already applied", idempotencyKey);
            return BalanceUpdateResult.ALREADY_APPLIED;
        }

        long startedAt = System.currentTimeMillis();
        int updated = 0;
        int lastId = Integer.MIN_VALUE;
        List<TimeDeposit> batch;
        while (!(batch = repository.findBatchAfterId(lastId, batchSize)).isEmpty()) {
            calculator.updateBalance(batch);
            repository.saveBalances(batch);
            updated += batch.size();
            lastId = batch.get(batch.size() - 1).getId();
            log.debug("Balance update batch saved: {} deposits, up to id {}", batch.size(), lastId);
        }

        if (idempotencyKey != null) {
            updateRuns.recordApplied(idempotencyKey);
        }
        log.info("Balance update applied to {} deposits in {} ms", updated, System.currentTimeMillis() - startedAt);
        return BalanceUpdateResult.APPLIED;
    }
}

