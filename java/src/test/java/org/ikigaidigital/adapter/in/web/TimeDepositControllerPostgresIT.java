package org.ikigaidigital.adapter.in.web;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Runs the same API contract with the "postgres" profile against a real PostgreSQL container,
 * verifying schema, seed data and queries are portable. Skipped automatically without Docker.
 */
@ActiveProfiles("postgres")
@Testcontainers(disabledWithoutDocker = true)
class TimeDepositControllerPostgresIT extends TimeDepositApiContract {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");
}

