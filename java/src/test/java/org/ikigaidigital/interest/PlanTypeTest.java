package org.ikigaidigital.interest;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PlanTypeTest {

    @Test
    void resolvesKnownCodes() {
        assertThat(PlanType.fromCode("basic")).contains(PlanType.BASIC);
        assertThat(PlanType.fromCode("student")).contains(PlanType.STUDENT);
        assertThat(PlanType.fromCode("premium")).contains(PlanType.PREMIUM);
    }

    @Test
    void unknownNullOrDifferentCaseCodesAreEmpty() {
        assertThat(PlanType.fromCode("gold")).isEmpty();
        assertThat(PlanType.fromCode(null)).isEmpty();
        assertThat(PlanType.fromCode("BASIC")).isEmpty();
    }

    @Test
    void everyPlanHasAPolicy() {
        for (PlanType planType : PlanType.values()) {
            assertThat(planType.interestPolicy()).isNotNull();
        }
    }
}

