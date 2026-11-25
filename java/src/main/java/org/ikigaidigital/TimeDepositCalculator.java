package org.ikigaidigital;

import org.ikigaidigital.interest.InterestPolicy;
import org.ikigaidigital.interest.InterestPolicyRegistry;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Shared calculator used across the application. Its public contract -
 * {@link #updateBalance(List)} - is unchanged from the original
 * implementation; only the internal plan-type conditionals were replaced
 * with a Strategy pattern ({@link InterestPolicy}) so that new plan types
 * can be added without touching this class (Open/Closed Principle).
 *
 * Behavior (including the rounding quirk via {@code new BigDecimal(double)})
 * is intentionally preserved to avoid any change in output.
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
        for (int i = 0; i < xs.size(); i++) {
            TimeDeposit timeDeposit = xs.get(i);

            InterestPolicy interestPolicy = interestPolicyRegistry.resolve(timeDeposit.getPlanType());
            double interest = interestPolicy.calculateMonthlyInterest(timeDeposit);

            double a2d = timeDeposit.getBalance() + (new BigDecimal(interest).setScale(2, RoundingMode.HALF_UP)).doubleValue();
            timeDeposit.setBalance(a2d);
        }
    }
}

