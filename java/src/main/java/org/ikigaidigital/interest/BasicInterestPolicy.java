package org.ikigaidigital.interest;

import org.ikigaidigital.TimeDeposit;

import static org.ikigaidigital.common.TimeDepositConstants.BASIC_ANNUAL_RATE;
import static org.ikigaidigital.common.TimeDepositConstants.INTEREST_FREE_DAYS;
import static org.ikigaidigital.common.TimeDepositConstants.MONTHS_PER_YEAR;

public class BasicInterestPolicy implements InterestPolicy {

    @Override
    public double calculateMonthlyInterest(TimeDeposit timeDeposit) {
        if (timeDeposit.getDays() <= INTEREST_FREE_DAYS) {
            return 0;
        }
        return timeDeposit.getBalance() * BASIC_ANNUAL_RATE / MONTHS_PER_YEAR;
    }
}

