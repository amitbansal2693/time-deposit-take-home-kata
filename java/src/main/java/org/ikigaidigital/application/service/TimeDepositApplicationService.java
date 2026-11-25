package org.ikigaidigital.application.service;

import org.ikigaidigital.TimeDeposit;
import org.ikigaidigital.TimeDepositCalculator;
import org.ikigaidigital.application.port.in.GetAllTimeDepositsUseCase;
import org.ikigaidigital.application.port.in.TimeDepositView;
import org.ikigaidigital.application.port.in.UpdateAllTimeDepositBalancesUseCase;
import org.ikigaidigital.application.port.out.TimeDepositRepositoryPort;
import org.ikigaidigital.domain.Withdrawal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Application service implementing both use cases. Delegates all plan-type
 * calculation logic to the shared, unmodified {@link TimeDepositCalculator}
 * contract - this class only orchestrates persistence and read-model
 * assembly.
 */
@Service
public class TimeDepositApplicationService implements GetAllTimeDepositsUseCase, UpdateAllTimeDepositBalancesUseCase {

    private final TimeDepositRepositoryPort repository;
    private final TimeDepositCalculator calculator;

    public TimeDepositApplicationService(TimeDepositRepositoryPort repository, TimeDepositCalculator calculator) {
        this.repository = repository;
        this.calculator = calculator;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TimeDepositView> getAllTimeDeposits() {
        List<TimeDeposit> deposits = repository.findAllTimeDeposits();
        List<Integer> ids = deposits.stream().map(TimeDeposit::getId).toList();
        Map<Integer, List<Withdrawal>> withdrawalsByDepositId = repository.findWithdrawalsGroupedByTimeDepositIds(ids);

        return deposits.stream()
                .map(deposit -> new TimeDepositView(
                        deposit,
                        withdrawalsByDepositId.getOrDefault(deposit.getId(), List.of())))
                .toList();
    }

    @Override
    @Transactional
    public void updateAllBalances() {
        List<TimeDeposit> deposits = repository.findAllTimeDeposits();
        // Reuses the shared, unmodified calculator contract.
        calculator.updateBalance(deposits);
        repository.saveBalances(deposits);
    }
}

