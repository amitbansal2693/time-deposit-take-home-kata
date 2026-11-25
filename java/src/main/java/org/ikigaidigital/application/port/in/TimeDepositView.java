package org.ikigaidigital.application.port.in;

import org.ikigaidigital.TimeDeposit;
import org.ikigaidigital.domain.Withdrawal;

import java.util.List;

/**
 * Read model combining the shared {@link TimeDeposit} with its withdrawals,
 * used only at the application boundary (never persisted, never replaces
 * the shared class).
 */
public record TimeDepositView(TimeDeposit timeDeposit, List<Withdrawal> withdrawals) {
}

