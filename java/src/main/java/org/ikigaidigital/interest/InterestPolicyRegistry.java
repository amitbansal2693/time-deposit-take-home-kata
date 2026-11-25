package org.ikigaidigital.interest;

import java.util.Map;

/**
 * Resolves the correct {@link InterestPolicy} for a given plan type.
 *
 * This is the single place that knows about plan-type <-> policy mapping.
 * Extending the system with a new plan type (e.g. "gold") only requires:
 *   1. Implementing a new InterestPolicy.
 *   2. Adding one entry to the map below.
 * No other class (controller, service, calculator) needs to change.
 */
public class InterestPolicyRegistry {

    private static final InterestPolicy DEFAULT_POLICY = new NoInterestPolicy();

    private final Map<String, InterestPolicy> policiesByPlanType = Map.of(
            "basic", new BasicInterestPolicy(),
            "student", new StudentInterestPolicy(),
            "premium", new PremiumInterestPolicy()
    );

    public InterestPolicy resolve(String planType) {
        if (planType == null) {
            return DEFAULT_POLICY;
        }
        return policiesByPlanType.getOrDefault(planType.toLowerCase(), DEFAULT_POLICY);
    }
}

