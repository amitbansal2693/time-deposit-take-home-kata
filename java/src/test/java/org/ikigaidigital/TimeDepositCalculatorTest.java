package org.ikigaidigital;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

public class TimeDepositCalculatorTest {

    private final TimeDepositCalculator calc = new TimeDepositCalculator();

    @Test
    public void updateBalance_Test() {
        List<TimeDeposit> plans = Arrays.asList(
            new TimeDeposit(1,"basic", 1234567.00, 45)
        );
        calc.updateBalance(plans);

        assertThat(1).isEqualTo(1);
    }

    @Test
    public void basicPlan_noInterest_within30Days() {
        List<TimeDeposit> plans = List.of(new TimeDeposit(1, "basic", 1000.0, 30));
        calc.updateBalance(plans);
        assertThat(plans.get(0).getBalance()).isEqualTo(1000.0);
    }

    @Test
    public void basicPlan_earnsInterest_afterDay30() {
        List<TimeDeposit> plans = List.of(new TimeDeposit(1, "basic", 1200.0, 45));
        calc.updateBalance(plans);
        assertThat(plans.get(0).getBalance()).isCloseTo(1201.0, within(0.01));
    }

    @Test
    public void studentPlan_noInterest_afterOneYear() {
        List<TimeDeposit> plans = List.of(new TimeDeposit(1, "student", 1000.0, 366));
        calc.updateBalance(plans);
        assertThat(plans.get(0).getBalance()).isEqualTo(1000.0);
    }

    @Test
    public void studentPlan_earnsInterest_beforeOneYear() {
        List<TimeDeposit> plans = List.of(new TimeDeposit(1, "student", 1200.0, 100));
        calc.updateBalance(plans);
        assertThat(plans.get(0).getBalance()).isCloseTo(1203.0, within(0.01));
    }

    @Test
    public void premiumPlan_noInterest_before45Days() {
        List<TimeDeposit> plans = List.of(new TimeDeposit(1, "premium", 1000.0, 45));
        calc.updateBalance(plans);
        assertThat(plans.get(0).getBalance()).isEqualTo(1000.0);
    }

    @Test
    public void premiumPlan_earnsInterest_after45Days() {
        List<TimeDeposit> plans = List.of(new TimeDeposit(1, "premium", 1200.0, 90));
        calc.updateBalance(plans);
        assertThat(plans.get(0).getBalance()).isCloseTo(1205.0, within(0.01));
    }

    @Test
    public void unknownPlanType_noInterest() {
        List<TimeDeposit> plans = List.of(new TimeDeposit(1, "gold", 1000.0, 100));
        calc.updateBalance(plans);
        assertThat(plans.get(0).getBalance()).isEqualTo(1000.0);
    }
}


