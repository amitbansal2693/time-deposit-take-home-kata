package org.ikigaidigital.interest;

import org.ikigaidigital.TimeDeposit;

/**
 * Strategy abstraction for computing the monthly interest of a single
 * {@link TimeDeposit}, isolated from the shared {@code TimeDepositCalculator}.
 *
 * Adding a new plan type in the future only requires a new implementation of
 * this interface plus a registration entry in {@link InterestPolicyRegistry};
 * no existing class needs to change (Open/Closed Principle).
 */
public interface InterestPolicy {

    /**
     * @return the raw (unrounded) monthly interest amount for the given deposit.
     *         Rounding/application to the balance is the caller's responsibility.
     */
    double calculateMonthlyInterest(TimeDeposit timeDeposit);
}

