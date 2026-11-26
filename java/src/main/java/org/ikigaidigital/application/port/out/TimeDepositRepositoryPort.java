package org.ikigaidigital.application.port.out;

import org.ikigaidigital.TimeDeposit;
import org.ikigaidigital.application.port.in.TimeDepositView;

import java.util.List;

public interface TimeDepositRepositoryPort {

    /** All deposits with their withdrawals, ordered by id, loaded in a single query. */
    List<TimeDepositView> findAllWithWithdrawals();

    /** Up to {@code limit} deposits with id greater than {@code afterId}, with withdrawals, ordered by id. */
    List<TimeDepositView> findPageWithWithdrawals(int afterId, int limit);

    /** Next batch of deposits with id greater than {@code lastId}, ordered by id (keyset pagination). */
    List<TimeDeposit> findBatchAfterId(int lastId, int batchSize);

    void saveBalances(List<TimeDeposit> timeDeposits);
}

