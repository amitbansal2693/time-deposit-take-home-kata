package org.ikigaidigital;

import org.ikigaidigital.interest.InterestPolicyRegistry;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import static org.ikigaidigital.common.TimeDepositConstants.MONEY_SCALE;

/**
 * Public contract and results are identical to the original implementation;
 * the plan-type if/else chain was replaced by {@link InterestPolicyRegistry}.
 */
public class TimeDepositCalculator {

    private final InterestPolicyRegistry interestPolicyRegistry;

    public TimeDepositCalculator() {
        this(new InterestPolicyRegistry());
    }

    public TimeDepositCalculator(InterestPolicyRegistry interestPolicyRegistry) {
        this.interestPolicyRegistry = interestPolicyRegistry;
    }

    public void updateBalance(List<TimeDeposit> xs) {
        for (TimeDeposit timeDeposit : xs) {
            double interest = interestPolicyRegistry.resolve(timeDeposit.getPlanType())
                    .calculateMonthlyInterest(timeDeposit);

            // new BigDecimal(double) is kept on purpose to reproduce the original rounding exactly.
            double a2d = timeDeposit.getBalance()
                    + new BigDecimal(interest).setScale(MONEY_SCALE, RoundingMode.HALF_UP).doubleValue();
            timeDeposit.setBalance(a2d);
        }
    }
}

