package org.ikigaidigital.interest;

import org.ikigaidigital.TimeDeposit;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class StudentInterestPolicyTest {

    private static final double BALANCE = 1200.0;
    private static final double EXPECTED_MONTHLY_INTEREST = BALANCE * 0.03 / 12;

    private final StudentInterestPolicy policy = new StudentInterestPolicy();

    // No interest in the first 30 days, nor from day 366 (one year) onwards: check both boundaries and nearby days.
    @ParameterizedTest
    @ValueSource(ints = {1, 29, 30, 366, 367, 400})
    void testCalculateMonthlyInterest_whenDaysOutsideEligibleWindow_thenZero(int days) {
        assertThat(policy.calculateMonthlyInterest(new TimeDeposit(1, "student", BALANCE, days))).isZero();
    }

    // Interest applies for 31..365 inclusive: check both ends of the eligible window and a day in the middle.
    @ParameterizedTest
    @ValueSource(ints = {31, 100, 364, 365})
    void testCalculateMonthlyInterest_whenDaysWithinEligibleWindow_thenThreePercentMonthly(int days) {
        double interest = policy.calculateMonthlyInterest(new TimeDeposit(1, "student", BALANCE, days));
        assertThat(interest).isCloseTo(EXPECTED_MONTHLY_INTEREST, within(1e-9));
    }
}
