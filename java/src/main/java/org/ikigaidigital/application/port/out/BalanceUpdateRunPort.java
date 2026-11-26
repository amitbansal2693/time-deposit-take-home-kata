package org.ikigaidigital.application.port.out;

/**
 * Guards the balance update against concurrent and duplicate runs.
 * All methods must be called inside the update transaction.
 */
public interface BalanceUpdateRunPort {

    /** Blocks until no other balance update holds the lock; released when the transaction ends. */
    void acquireExclusiveLock();

    boolean isAlreadyApplied(String idempotencyKey);

    void recordApplied(String idempotencyKey);
}


