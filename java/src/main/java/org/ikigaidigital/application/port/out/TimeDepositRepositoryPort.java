package org.ikigaidigital.application.port.out;

import org.ikigaidigital.TimeDeposit;
import org.ikigaidigital.domain.Withdrawal;

import java.util.List;
import java.util.Map;

/**
 * Outbound port implemented by the persistence adapter. Keeps the
 * application layer free of JPA/database concerns.
 */
public interface TimeDepositRepositoryPort {

    List<TimeDeposit> findAllTimeDeposits();

    Map<Integer, List<Withdrawal>> findWithdrawalsGroupedByTimeDepositIds(List<Integer> timeDepositIds);

    void saveBalances(List<TimeDeposit> timeDeposits);
}

