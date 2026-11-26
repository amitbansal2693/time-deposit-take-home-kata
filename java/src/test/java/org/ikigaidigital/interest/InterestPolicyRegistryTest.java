package org.ikigaidigital.interest;

import org.ikigaidigital.TimeDeposit;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InterestPolicyRegistryTest {

    private final InterestPolicyRegistry registry = new InterestPolicyRegistry();

    @Test
    void resolvesKnownPlanTypes() {
        assertThat(registry.resolve("basic")).isInstanceOf(BasicInterestPolicy.class);
        assertThat(registry.resolve("student")).isInstanceOf(StudentInterestPolicy.class);
        assertThat(registry.resolve("premium")).isInstanceOf(PremiumInterestPolicy.class);
    }

    @Test
    void matchesPlanTypesExactly_likeTheOriginalCalculator() {
        assertThat(registry.resolve("STUDENT")).isInstanceOf(NoInterestPolicy.class);
    }

    @Test
    void fallsBackToNoInterest_forUnknownOrNullPlanType() {
        assertThat(registry.resolve("gold")).isInstanceOf(NoInterestPolicy.class);
        assertThat(registry.resolve(null)).isInstanceOf(NoInterestPolicy.class);
        assertThat(registry.resolve(null).calculateMonthlyInterest(new TimeDeposit(1, "gold", 1000.0, 100))).isZero();
    }
}

