package org.ikigaidigital.adapter.out.persistence;

import jakarta.persistence.EntityManager;
import org.ikigaidigital.TimeDeposit;
import org.ikigaidigital.application.port.in.TimeDepositView;
import org.ikigaidigital.application.port.out.TimeDepositRepositoryPort;
import org.ikigaidigital.domain.Withdrawal;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import static org.ikigaidigital.common.TimeDepositConstants.MONEY_SCALE;

/**
 * Precision trade-off: balances are stored as DECIMAL but the shared TimeDeposit uses Double,
 * so values are converted at this boundary and rounded back to 2 decimals on save.
 * Calculation therefore inherits the original calculator's double arithmetic.
 */
@Repository
public class TimeDepositPersistenceAdapter implements TimeDepositRepositoryPort {

    private final TimeDepositJpaRepository repository;
    private final EntityManager entityManager;

    public TimeDepositPersistenceAdapter(TimeDepositJpaRepository repository, EntityManager entityManager) {
        this.repository = repository;
        this.entityManager = entityManager;
    }

    @Override
    public List<TimeDepositView> findAllWithWithdrawals() {
        return toViews(repository.findAllWithWithdrawalsOrderById());
    }

    @Override
    public List<TimeDepositView> findPageWithWithdrawals(int afterId, int limit) {
        List<Integer> ids = repository.findIdsAfter(afterId, PageRequest.of(0, limit));
        return ids.isEmpty() ? List.of() : toViews(repository.findWithWithdrawalsByIdIn(ids));
    }

    private List<TimeDepositView> toViews(List<TimeDepositJpaEntity> entities) {
        return entities.stream()
                .map(entity -> new TimeDepositView(toDomain(entity), toWithdrawals(entity)))
                .toList();
    }

    @Override
    public List<TimeDeposit> findBatchAfterId(int lastId, int batchSize) {
        return repository.findByIdGreaterThanOrderByIdAsc(lastId, Limit.of(batchSize)).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public void saveBalances(List<TimeDeposit> timeDeposits) {
        for (TimeDeposit deposit : timeDeposits) {
            // Entities of the current batch are already managed, so find() hits the persistence context.
            TimeDepositJpaEntity entity = entityManager.find(TimeDepositJpaEntity.class, deposit.getId());
            entity.setBalance(BigDecimal.valueOf(deposit.getBalance()).setScale(MONEY_SCALE, RoundingMode.HALF_UP));
        }
        entityManager.flush();  // sent as JDBC batch (hibernate.jdbc.batch_size)
        entityManager.clear();  // release the batch to keep memory bounded
    }

    private TimeDeposit toDomain(TimeDepositJpaEntity entity) {
        return new TimeDeposit(entity.getId(), entity.getPlanType(), entity.getBalance().doubleValue(), entity.getDays());
    }

    private List<Withdrawal> toWithdrawals(TimeDepositJpaEntity entity) {
        return entity.getWithdrawals().stream()
                .map(w -> new Withdrawal(w.getId(), w.getTimeDepositId(), w.getAmount(), w.getDate()))
                .toList();
    }
}

