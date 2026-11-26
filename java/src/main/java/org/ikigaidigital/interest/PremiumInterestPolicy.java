package org.ikigaidigital.interest;

import org.ikigaidigital.TimeDeposit;

import static org.ikigaidigital.common.TimeDepositConstants.MONTHS_PER_YEAR;
import static org.ikigaidigital.common.TimeDepositConstants.PREMIUM_ANNUAL_RATE;
import static org.ikigaidigital.common.TimeDepositConstants.PREMIUM_INTEREST_FREE_DAYS;

public class PremiumInterestPolicy implements InterestPolicy {

    @Override
    public double calculateMonthlyInterest(TimeDeposit timeDeposit) {
        if (timeDeposit.getDays() <= PREMIUM_INTEREST_FREE_DAYS) {
            return 0;
        }
        return timeDeposit.getBalance() * PREMIUM_ANNUAL_RATE / MONTHS_PER_YEAR;
    }
}

