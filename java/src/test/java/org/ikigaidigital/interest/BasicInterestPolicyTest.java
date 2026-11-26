package org.ikigaidigital.interest;

import org.ikigaidigital.TimeDeposit;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class BasicInterestPolicyTest {

    private final BasicInterestPolicy policy = new BasicInterestPolicy();

    @Test
    void calculateMonthlyInterest_whenOnOrBeforeDay30_thenZero() {
        assertThat(policy.calculateMonthlyInterest(new TimeDeposit(1, "basic", 1000.0, 30))).isZero();
    }

    @Test
    void calculateMonthlyInterest_whenAfterDay30_thenOnePercentMonthly() {
        double interest = policy.calculateMonthlyInterest(new TimeDeposit(1, "basic", 1200.0, 45));
        assertThat(interest).isCloseTo(1200.0 * 0.01 / 12, within(1e-9));
    }
}

