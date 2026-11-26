package org.ikigaidigital.interest;

import org.ikigaidigital.TimeDeposit;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class BasicInterestPolicyTest {

    private static final double BALANCE = 1200.0;
    private static final double EXPECTED_MONTHLY_INTEREST = BALANCE * 0.01 / 12;

    private final BasicInterestPolicy policy = new BasicInterestPolicy();

    // Interest-free window is days <= 30: check well inside it, the day before and the exact boundary.
    @ParameterizedTest
    @ValueSource(ints = {1, 15, 29, 30})
    void testCalculateMonthlyInterest_whenDaysOnOrBeforeInterestFreeBoundary_thenZero(int days) {
        assertThat(policy.calculateMonthlyInterest(new TimeDeposit(1, "basic", BALANCE, days))).isZero();
    }

    // Interest applies from day 31 onwards: check the first eligible day and days further out.
    @ParameterizedTest
    @ValueSource(ints = {31, 45, 365, 1000})
    void testCalculateMonthlyInterest_whenDaysAfterInterestFreeBoundary_thenOnePercentMonthly(int days) {
        double interest = policy.calculateMonthlyInterest(new TimeDeposit(1, "basic", BALANCE, days));
        assertThat(interest).isCloseTo(EXPECTED_MONTHLY_INTEREST, within(1e-9));
    }
}
