package org.ikigaidigital.interest;

import org.ikigaidigital.TimeDeposit;

/**
 * Fallback policy for unknown/unsupported plan types. Mirrors the original
 * junior-developer implementation, where an unrecognised plan simply earns
 * no interest instead of throwing an exception (per assignment guideline:
 * "Handling invalid input or exceptions is not required").
 */
public class NoInterestPolicy implements InterestPolicy {

    @Override
    public double calculateMonthlyInterest(TimeDeposit timeDeposit) {
        return 0;
    }
}

