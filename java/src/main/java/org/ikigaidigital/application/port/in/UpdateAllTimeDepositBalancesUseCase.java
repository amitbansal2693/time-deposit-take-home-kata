package org.ikigaidigital.application.port.in;

/**
 * Inbound port for the balance-update endpoint. Applies one monthly
 * interest calculation (via the shared {@code TimeDepositCalculator}) to
 * every stored time deposit and persists the resulting balances.
 */
public interface UpdateAllTimeDepositBalancesUseCase {

    void updateAllBalances();
}

