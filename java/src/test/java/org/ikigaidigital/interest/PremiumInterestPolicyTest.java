package org.ikigaidigital.interest;

import org.ikigaidigital.TimeDeposit;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class PremiumInterestPolicyTest {

    private final PremiumInterestPolicy policy = new PremiumInterestPolicy();

    @Test
    void noInterest_onOrBeforeDay45() {
        assertThat(policy.calculateMonthlyInterest(new TimeDeposit(1, "premium", 1000.0, 45))).isZero();
    }

    @Test
    void interestApplied_afterDay45() {
        double interest = policy.calculateMonthlyInterest(new TimeDeposit(1, "premium", 1200.0, 46));
        assertThat(interest).isCloseTo(1200.0 * 0.05 / 12, within(1e-9));
    }
}

