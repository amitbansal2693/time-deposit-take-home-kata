package org.ikigaidigital.adapter.out.persistence;

import org.ikigaidigital.TimeDeposit;
import org.ikigaidigital.application.port.out.TimeDepositRepositoryPort;
import org.ikigaidigital.domain.Withdrawal;
import org.springframework.stereotype.Repository;

import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Translates between the persistence model (JPA entities) and the domain
 * model ({@link TimeDeposit}, {@link Withdrawal}), keeping JPA concerns out
 * of the application and domain layers (hexagonal architecture).
 */
@Repository
public class TimeDepositPersistenceAdapter implements TimeDepositRepositoryPort {

    private final TimeDepositJpaRepository timeDepositJpaRepository;
    private final WithdrawalJpaRepository withdrawalJpaRepository;

    public TimeDepositPersistenceAdapter(TimeDepositJpaRepository timeDepositJpaRepository,
                                          WithdrawalJpaRepository withdrawalJpaRepository) {
        this.timeDepositJpaRepository = timeDepositJpaRepository;
        this.withdrawalJpaRepository = withdrawalJpaRepository;
    }

    @Override
    public List<TimeDeposit> findAllTimeDeposits() {
        return timeDepositJpaRepository.findAll().stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Map<Integer, List<Withdrawal>> findWithdrawalsGroupedByTimeDepositIds(List<Integer> timeDepositIds) {
        if (timeDepositIds.isEmpty()) {
            return Map.of();
        }
        return withdrawalJpaRepository.findByTimeDepositIdIn(timeDepositIds).stream()
                .map(this::toDomain)
                .collect(Collectors.groupingBy(Withdrawal::getTimeDepositId));
    }

    @Override
    public void saveBalances(List<TimeDeposit> timeDeposits) {
        List<Integer> ids = timeDeposits.stream().map(TimeDeposit::getId).toList();
        Map<Integer, TimeDepositJpaEntity> entitiesById = timeDepositJpaRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(TimeDepositJpaEntity::getId, entity -> entity));

        for (TimeDeposit timeDeposit : timeDeposits) {
            TimeDepositJpaEntity entity = entitiesById.get(timeDeposit.getId());
            if (entity != null) {
                entity.setBalance(java.math.BigDecimal.valueOf(timeDeposit.getBalance())
                        .setScale(2, RoundingMode.HALF_UP));
            }
        }
        timeDepositJpaRepository.saveAll(entitiesById.values());
    }

    private TimeDeposit toDomain(TimeDepositJpaEntity entity) {
        return new TimeDeposit(entity.getId(), entity.getPlanType(), entity.getBalance().doubleValue(), entity.getDays());
    }

    private Withdrawal toDomain(WithdrawalJpaEntity entity) {
        return new Withdrawal(entity.getId(), entity.getTimeDepositId(), entity.getAmount(), entity.getDate());
    }
}

