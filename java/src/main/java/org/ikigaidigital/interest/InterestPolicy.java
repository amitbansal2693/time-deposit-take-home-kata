package org.ikigaidigital.interest;

import org.ikigaidigital.TimeDeposit;

/**
 * Strategy for one plan type. Returns the raw (unrounded) monthly interest.
 */
public interface InterestPolicy {

    double calculateMonthlyInterest(TimeDeposit timeDeposit);
}

