package org.ikigaidigital.interest;

import org.ikigaidigital.TimeDeposit;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class PremiumInterestPolicyTest {

    private static final double BALANCE = 1200.0;
    private static final double EXPECTED_MONTHLY_INTEREST = BALANCE * 0.05 / 12;

    private final PremiumInterestPolicy policy = new PremiumInterestPolicy();

    // Premium earns nothing until after day 45: check the 30-day mark, the day before the boundary and day 45 itself.
    @ParameterizedTest
    @ValueSource(ints = {1, 30, 44, 45})
    void testCalculateMonthlyInterest_whenDaysOnOrBeforeDay45_thenZero(int days) {
        assertThat(policy.calculateMonthlyInterest(new TimeDeposit(1, "premium", BALANCE, days))).isZero();
    }

    // Interest applies from day 46 onwards: check the first eligible day and days further out.
    @ParameterizedTest
    @ValueSource(ints = {46, 90, 365})
    void testCalculateMonthlyInterest_whenDaysAfterDay45_thenFivePercentMonthly(int days) {
        double interest = policy.calculateMonthlyInterest(new TimeDeposit(1, "premium", BALANCE, days));
        assertThat(interest).isCloseTo(EXPECTED_MONTHLY_INTEREST, within(1e-9));
    }
}
