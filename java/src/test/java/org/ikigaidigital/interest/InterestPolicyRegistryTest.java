package org.ikigaidigital.interest;

import org.ikigaidigital.TimeDeposit;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InterestPolicyRegistryTest {

    private final InterestPolicyRegistry registry = new InterestPolicyRegistry();

    @Test
    void resolvesKnownPlanTypes_caseInsensitively() {
        assertThat(registry.resolve("basic")).isInstanceOf(BasicInterestPolicy.class);
        assertThat(registry.resolve("STUDENT")).isInstanceOf(StudentInterestPolicy.class);
        assertThat(registry.resolve("Premium")).isInstanceOf(PremiumInterestPolicy.class);
    }

    @Test
    void fallsBackToNoInterest_forUnknownOrNullPlanType() {
        assertThat(registry.resolve("gold")).isInstanceOf(NoInterestPolicy.class);
        assertThat(registry.resolve(null)).isInstanceOf(NoInterestPolicy.class);
        assertThat(registry.resolve(null).calculateMonthlyInterest(new TimeDeposit(1, "gold", 1000.0, 100))).isZero();
    }
}

