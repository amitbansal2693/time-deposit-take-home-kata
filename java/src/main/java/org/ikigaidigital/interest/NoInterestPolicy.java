package org.ikigaidigital.interest;

import org.ikigaidigital.TimeDeposit;

/**
 * Unknown plan types earn no interest, matching the original calculator.
 */
public class NoInterestPolicy implements InterestPolicy {

    @Override
    public double calculateMonthlyInterest(TimeDeposit timeDeposit) {
        return 0;
    }
}

