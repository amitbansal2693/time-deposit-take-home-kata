package org.ikigaidigital.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.ShallowEtagHeaderFilter;

import static org.ikigaidigital.common.TimeDepositConstants.API_BASE_PATH;

@Configuration
public class WebConfig {

    /**
     * Adds an ETag to GET responses; a client sending If-None-Match with an unchanged body gets
     * 304 without the payload. Responses marked no-store (stale fallbacks) are skipped.
     */
    @Bean
    FilterRegistrationBean<ShallowEtagHeaderFilter> etagFilter() {
        FilterRegistrationBean<ShallowEtagHeaderFilter> registration = new FilterRegistrationBean<>(new ShallowEtagHeaderFilter());
        registration.addUrlPatterns(API_BASE_PATH + "/*");
        return registration;
    }
}


