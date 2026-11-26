package org.ikigaidigital.interest;

import org.ikigaidigital.TimeDeposit;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class PremiumInterestPolicyTest {

    private final PremiumInterestPolicy policy = new PremiumInterestPolicy();

    @Test
    void calculateMonthlyInterest_whenOnOrBeforeDay45_thenZero() {
        assertThat(policy.calculateMonthlyInterest(new TimeDeposit(1, "premium", 1000.0, 45))).isZero();
    }

    @Test
    void calculateMonthlyInterest_whenAfterDay45_thenFivePercentMonthly() {
        double interest = policy.calculateMonthlyInterest(new TimeDeposit(1, "premium", 1200.0, 46));
        assertThat(interest).isCloseTo(1200.0 * 0.05 / 12, within(1e-9));
    }
}

