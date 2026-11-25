package org.ikigaidigital.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI timeDepositOpenApi() {
        return new OpenAPI().info(new Info()
                .title("XA Bank Time Deposit API")
                .description("Exactly two endpoints: retrieve all time deposits, and update all balances.")
                .version("v1"));
    }
}

