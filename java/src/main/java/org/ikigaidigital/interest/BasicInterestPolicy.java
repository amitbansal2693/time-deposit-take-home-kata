package org.ikigaidigital.interest;

import org.ikigaidigital.TimeDeposit;

/**
 * Basic Plan: 1% annual interest, applied monthly, starting after the
 * first 30 days (strictly more than 30 days held).
 */
public class BasicInterestPolicy implements InterestPolicy {

    private static final double ANNUAL_RATE = 0.01;
    private static final int MIN_DAYS_EXCLUSIVE = 30;

    @Override
    public double calculateMonthlyInterest(TimeDeposit timeDeposit) {
        if (timeDeposit.getDays() <= MIN_DAYS_EXCLUSIVE) {
            return 0;
        }
        return timeDeposit.getBalance() * ANNUAL_RATE / 12;
    }
}

