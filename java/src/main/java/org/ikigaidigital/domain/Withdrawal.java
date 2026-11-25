package org.ikigaidigital.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Immutable domain representation of a withdrawal made against a time
 * deposit. Kept separate from the shared {@code TimeDeposit} class so that
 * class is never modified.
 */
public final class Withdrawal {

    private final int id;
    private final int timeDepositId;
    private final BigDecimal amount;
    private final LocalDate date;

    public Withdrawal(int id, int timeDepositId, BigDecimal amount, LocalDate date) {
        this.id = id;
        this.timeDepositId = timeDepositId;
        this.amount = amount;
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public int getTimeDepositId() {
        return timeDepositId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public LocalDate getDate() {
        return date;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Withdrawal that)) return false;
        return id == that.id && timeDepositId == that.timeDepositId
                && Objects.equals(amount, that.amount) && Objects.equals(date, that.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, timeDepositId, amount, date);
    }
}

