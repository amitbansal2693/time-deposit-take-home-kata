package org.ikigaidigital.interest;

import org.ikigaidigital.TimeDeposit;

import static org.ikigaidigital.common.TimeDepositConstants.INTEREST_FREE_DAYS;
import static org.ikigaidigital.common.TimeDepositConstants.MONTHS_PER_YEAR;
import static org.ikigaidigital.common.TimeDepositConstants.STUDENT_ANNUAL_RATE;
import static org.ikigaidigital.common.TimeDepositConstants.STUDENT_MAX_DAYS_EXCLUSIVE;

public class StudentInterestPolicy implements InterestPolicy {

    @Override
    public double calculateMonthlyInterest(TimeDeposit timeDeposit) {
        int days = timeDeposit.getDays();
        if (days <= INTEREST_FREE_DAYS || days >= STUDENT_MAX_DAYS_EXCLUSIVE) {
            return 0;
        }
        return timeDeposit.getBalance() * STUDENT_ANNUAL_RATE / MONTHS_PER_YEAR;
    }
}

