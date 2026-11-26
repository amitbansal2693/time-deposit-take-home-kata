package org.ikigaidigital.interest;

/**
 * Resolves the interest policy for a plan-type code. Unknown or null plans earn no interest.
 * Kept as the calculator's dependency so the lookup strategy can change without touching the calculator.
 */
public class InterestPolicyRegistry {

    private static final InterestPolicy NO_INTEREST = new NoInterestPolicy();

    public InterestPolicy resolve(String planType) {
        return PlanType.fromCode(planType)
                .map(PlanType::interestPolicy)
                .orElse(NO_INTEREST);
    }
}

