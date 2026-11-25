package org.ikigaidigital.config;

import org.ikigaidigital.TimeDepositCalculator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Exposes the shared, framework-agnostic {@link TimeDepositCalculator} as a
 * Spring bean without annotating the shared class itself, so it remains
 * usable outside of a Spring context too (e.g. in the existing unit test).
 */
@Configuration
public class TimeDepositCalculatorConfig {

    @Bean
    public TimeDepositCalculator timeDepositCalculator() {
        return new TimeDepositCalculator();
    }
}

