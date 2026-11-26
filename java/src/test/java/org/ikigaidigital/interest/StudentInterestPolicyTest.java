package org.ikigaidigital.interest;

import org.ikigaidigital.TimeDeposit;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class StudentInterestPolicyTest {

    private final StudentInterestPolicy policy = new StudentInterestPolicy();

    @Test
    void testCalculateMonthlyInterest_whenOnOrBeforeDay30_thenZero() {
        assertThat(policy.calculateMonthlyInterest(new TimeDeposit(1, "student", 1000.0, 30))).isZero();
    }

    @Test
    void testCalculateMonthlyInterest_whenBetweenDay31AndDay365_thenThreePercentMonthly() {
        double interest = policy.calculateMonthlyInterest(new TimeDeposit(1, "student", 1200.0, 365));
        assertThat(interest).isCloseTo(1200.0 * 0.03 / 12, within(1e-9));
    }

    @Test
    void testCalculateMonthlyInterest_whenOnOrAfterDay366_thenZero() {
        assertThat(policy.calculateMonthlyInterest(new TimeDeposit(1, "student", 1000.0, 366))).isZero();
    }
}
