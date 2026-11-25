package org.ikigaidigital.interest;

import org.ikigaidigital.TimeDeposit;

/**
 * Student Plan: 3% annual interest, applied monthly, starting after the
 * first 30 days and stopping once the deposit has been held for 1 year
 * (366 days or more, matching the original business rule).
 */
public class StudentInterestPolicy implements InterestPolicy {

    private static final double ANNUAL_RATE = 0.03;
    private static final int MIN_DAYS_EXCLUSIVE = 30;
    private static final int MAX_DAYS_EXCLUSIVE = 366;

    @Override
    public double calculateMonthlyInterest(TimeDeposit timeDeposit) {
        int days = timeDeposit.getDays();
        if (days <= MIN_DAYS_EXCLUSIVE || days >= MAX_DAYS_EXCLUSIVE) {
            return 0;
        }
        return timeDeposit.getBalance() * ANNUAL_RATE / 12;
    }
}

