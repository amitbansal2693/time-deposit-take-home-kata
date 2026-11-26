package org.ikigaidigital.interest;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.ikigaidigital.common.TimeDepositConstants.PLAN_BASIC;
import static org.ikigaidigital.common.TimeDepositConstants.PLAN_PREMIUM;
import static org.ikigaidigital.common.TimeDepositConstants.PLAN_STUDENT;

/**
 * Supported plan types and their interest policy. Adding a plan = one new constant + one policy class.
 * Codes are matched exactly (case-sensitive), as in the original calculator.
 */
public enum PlanType {

    BASIC(PLAN_BASIC, new BasicInterestPolicy()),
    STUDENT(PLAN_STUDENT, new StudentInterestPolicy()),
    PREMIUM(PLAN_PREMIUM, new PremiumInterestPolicy());

    private static final Map<String, PlanType> BY_CODE = Arrays.stream(values())
            .collect(Collectors.toUnmodifiableMap(PlanType::code, Function.identity()));

    private final String code;
    private final InterestPolicy interestPolicy;

    PlanType(String code, InterestPolicy interestPolicy) {
        this.code = code;
        this.interestPolicy = interestPolicy;
    }

    public String code() {
        return code;
    }

    public InterestPolicy interestPolicy() {
        return interestPolicy;
    }

    public static Optional<PlanType> fromCode(String code) {
        return Optional.ofNullable(code).map(BY_CODE::get);
    }
}

