package org.ikigaidigital;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

public class TimeDepositCalculatorTest {

    private final TimeDepositCalculator calc = new TimeDepositCalculator();

    @Test
    public void testUpdateBalance_whenBasicPlanWith45Days_thenMatchesOriginalRoundedBalance() {
        List<TimeDeposit> plans = Arrays.asList(
            new TimeDeposit(1,"basic", 1234567.00, 45)
        );
        calc.updateBalance(plans);

        assertThat(plans.get(0).getBalance()).isEqualTo(1235595.81);
    }

    @Test
    public void testUpdateBalance_whenPlanTypeCaseDiffers_thenNoInterest() {
        List<TimeDeposit> plans = List.of(new TimeDeposit(1, "BASIC", 1200.0, 45));
        calc.updateBalance(plans);
        assertThat(plans.get(0).getBalance()).isEqualTo(1200.0);
    }

    @Test
    public void testUpdateBalance_whenBasicPlanOnDay30_thenNoInterest() {
        List<TimeDeposit> plans = List.of(new TimeDeposit(1, "basic", 1000.0, 30));
        calc.updateBalance(plans);
        assertThat(plans.get(0).getBalance()).isEqualTo(1000.0);
    }

    @Test
    public void testUpdateBalance_whenBasicPlanAfterDay30_thenInterestApplied() {
        List<TimeDeposit> plans = List.of(new TimeDeposit(1, "basic", 1200.0, 45));
        calc.updateBalance(plans);
        assertThat(plans.get(0).getBalance()).isCloseTo(1201.0, within(0.01));
    }

    @Test
    public void testUpdateBalance_whenStudentPlanAfterOneYear_thenNoInterest() {
        List<TimeDeposit> plans = List.of(new TimeDeposit(1, "student", 1000.0, 366));
        calc.updateBalance(plans);
        assertThat(plans.get(0).getBalance()).isEqualTo(1000.0);
    }

    @Test
    public void testUpdateBalance_whenStudentPlanBeforeOneYear_thenInterestApplied() {
        List<TimeDeposit> plans = List.of(new TimeDeposit(1, "student", 1200.0, 100));
        calc.updateBalance(plans);
        assertThat(plans.get(0).getBalance()).isCloseTo(1203.0, within(0.01));
    }

    @Test
    public void testUpdateBalance_whenPremiumPlanOnDay45_thenNoInterest() {
        List<TimeDeposit> plans = List.of(new TimeDeposit(1, "premium", 1000.0, 45));
        calc.updateBalance(plans);
        assertThat(plans.get(0).getBalance()).isEqualTo(1000.0);
    }

    @Test
    public void testUpdateBalance_whenPremiumPlanAfterDay45_thenInterestApplied() {
        List<TimeDeposit> plans = List.of(new TimeDeposit(1, "premium", 1200.0, 90));
        calc.updateBalance(plans);
        assertThat(plans.get(0).getBalance()).isCloseTo(1205.0, within(0.01));
    }

    @Test
    public void testUpdateBalance_whenUnknownPlanType_thenNoInterest() {
        List<TimeDeposit> plans = List.of(new TimeDeposit(1, "gold", 1000.0, 100));
        calc.updateBalance(plans);
        assertThat(plans.get(0).getBalance()).isEqualTo(1000.0);
    }
}
