package org.ikigaidigital.interest;

import org.ikigaidigital.TimeDeposit;

/**
 * Premium Plan: 5% annual interest, applied monthly, starting only after
 * the deposit has been held for more than 45 days (stricter than the
 * general 30-day rule).
 */
public class PremiumInterestPolicy implements InterestPolicy {

    private static final double ANNUAL_RATE = 0.05;
    private static final int MIN_DAYS_EXCLUSIVE = 45;

    @Override
    public double calculateMonthlyInterest(TimeDeposit timeDeposit) {
        if (timeDeposit.getDays() <= MIN_DAYS_EXCLUSIVE) {
            return 0;
        }
        return timeDeposit.getBalance() * ANNUAL_RATE / 12;
    }
}

