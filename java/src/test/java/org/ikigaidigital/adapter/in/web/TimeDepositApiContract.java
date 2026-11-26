package org.ikigaidigital.adapter.in.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * API tests shared by every database the application supports.
 * Each test runs in a rolled-back transaction and starts with empty caches,
 * so tests are independent of execution order.
 */
@SpringBootTest(properties = {
        "resilience4j.ratelimiter.instances.timeDepositsUpdate.limit-for-period=1000",
        "time-deposit.api.max-idempotency-key-length=20"})
@AutoConfigureMockMvc
@Transactional
abstract class TimeDepositApiContract {

    private static final String DEPOSITS = "/api/v1/time-deposits";
    private static final String BALANCES = DEPOSITS + "/balances";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void clearCaches() {
        cacheManager.getCacheNames().forEach(name -> Objects.requireNonNull(cacheManager.getCache(name)).clear());
    }

    @Test
    void getReturnsAllDepositsOrderedByIdWithWithdrawals() throws Exception {
        mockMvc.perform(get(DEPOSITS))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(6))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].planType").value("basic"))
                .andExpect(jsonPath("$[0].balance").value(1234567.0))
                .andExpect(jsonPath("$[0].days").value(45))
                .andExpect(jsonPath("$[0].withdrawals[0].amount").value(500.0))
                .andExpect(jsonPath("$[2].withdrawals.length()").value(0))
                .andExpect(jsonPath("$[5].id").value(6))
                .andExpect(header().doesNotExist("X-Next-After-Id"));
    }

    @Test
    void putAppliesMonthlyInterestPerPlanAndPersists() throws Exception {
        mockMvc.perform(put(BALANCES))
                .andExpect(status().isNoContent())
                .andExpect(header().string("Idempotency-Replayed", "false"));

        mockMvc.perform(get(DEPOSITS))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].balance").value(1235595.81)) // basic, 45 days
                .andExpect(jsonPath("$[1].balance").value(5012.5))     // student, 100 days
                .andExpect(jsonPath("$[2].balance").value(20000.0))    // premium, 10 days: none
                .andExpect(jsonPath("$[3].balance").value(15062.5))    // premium, 90 days
                .andExpect(jsonPath("$[4].balance").value(3000.0))     // student, 400 days: none
                .andExpect(jsonPath("$[5].balance").value(1000.0));    // basic, 10 days: none
    }

    @Test
    void putEvictsCachedGetResponse() throws Exception {
        mockMvc.perform(get(DEPOSITS)).andExpect(jsonPath("$[0].balance").value(1234567.0));

        mockMvc.perform(put(BALANCES)).andExpect(status().isNoContent());

        mockMvc.perform(get(DEPOSITS)).andExpect(jsonPath("$[0].balance").value(1235595.81));
    }

    @Test
    void putWithSameIdempotencyKeyAppliesInterestOnce() throws Exception {
        mockMvc.perform(put(BALANCES).header("Idempotency-Key", "run-2026-11"))
                .andExpect(status().isNoContent())
                .andExpect(header().string("Idempotency-Replayed", "false"));
        mockMvc.perform(put(BALANCES).header("Idempotency-Key", "run-2026-11"))
                .andExpect(status().isNoContent())
                .andExpect(header().string("Idempotency-Replayed", "true"));

        mockMvc.perform(get(DEPOSITS)).andExpect(jsonPath("$[0].balance").value(1235595.81));
    }

    @Test
    void putEnforcesConfiguredIdempotencyKeyMaxLength() throws Exception {
        // max-idempotency-key-length=20 for this test class
        mockMvc.perform(put(BALANCES).header("Idempotency-Key", "k".repeat(21)))
                .andExpect(status().isBadRequest());
        mockMvc.perform(put(BALANCES).header("Idempotency-Key", "k".repeat(20)))
                .andExpect(status().isNoContent());
    }

    @Test
    void getSupportsKeysetPagination() throws Exception {
        mockMvc.perform(get(DEPOSITS).param("limit", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].withdrawals[0].amount").value(500.0))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(header().string("X-Next-After-Id", "2"));

        mockMvc.perform(get(DEPOSITS).param("afterId", "4").param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(5))
                .andExpect(jsonPath("$[1].id").value(6))
                .andExpect(header().doesNotExist("X-Next-After-Id"));
    }

    @Test
    void getReturnsEtagAndNotModifiedWhenUnchanged() throws Exception {
        String etag = mockMvc.perform(get(DEPOSITS))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ETAG, notNullValue()))
                .andReturn().getResponse().getHeader(HttpHeaders.ETAG);

        mockMvc.perform(get(DEPOSITS).header(HttpHeaders.IF_NONE_MATCH, etag))
                .andExpect(status().isNotModified());
    }

    @Test
    void testGetTimeDeposits_whenCorrelationIdHeader_thenEchoedOtherwiseGenerated() throws Exception {
        mockMvc.perform(get(DEPOSITS).header("X-Correlation-Id", "abc-123"))
                .andExpect(header().string("X-Correlation-Id", "abc-123"));
        mockMvc.perform(get(DEPOSITS))
                .andExpect(header().string("X-Correlation-Id", notNullValue()));
    }
}
