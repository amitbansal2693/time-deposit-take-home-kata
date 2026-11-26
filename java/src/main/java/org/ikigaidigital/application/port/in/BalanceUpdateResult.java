package org.ikigaidigital.application.port.in;

public enum BalanceUpdateResult {
    /** Interest was applied to every deposit. */
    APPLIED,
    /** The idempotency key was already used; balances were left unchanged. */
    ALREADY_APPLIED
}


