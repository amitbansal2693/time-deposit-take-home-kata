package org.ikigaidigital.adapter.in.web;

import org.ikigaidigital.TimeDeposit;
import org.ikigaidigital.application.port.in.BalanceUpdateResult;
import org.ikigaidigital.application.port.in.GetAllTimeDepositsUseCase;
import org.ikigaidigital.application.port.in.TimeDepositView;
import org.ikigaidigital.application.port.in.UpdateAllTimeDepositBalancesUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Objects;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web-layer resilience: stale fallback when the database fails, and rate limiting.
 * Use cases are mocked so failures can be simulated without touching the database.
 */
@SpringBootTest(properties = "resilience4j.ratelimiter.instances.timeDepositsUpdate.limit-for-period=1")
@AutoConfigureMockMvc
class TimeDepositResilienceIT {

    private static final String DEPOSITS = "/api/v1/time-deposits";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CacheManager cacheManager;

    @MockBean
    private GetAllTimeDepositsUseCase getTimeDeposits;

    @MockBean
    private UpdateAllTimeDepositBalancesUseCase updateAllBalances;

    @BeforeEach
    void clearCaches() {
        cacheManager.getCacheNames().forEach(name -> Objects.requireNonNull(cacheManager.getCache(name)).clear());
    }

    @Test
    void servesLastKnownResponseMarkedStaleWhenDatabaseFails() throws Exception {
        when(getTimeDeposits.getAllTimeDeposits())
                .thenReturn(List.of(new TimeDepositView(new TimeDeposit(1, "basic", 100.0, 45), List.of())))
                .thenThrow(new DataAccessResourceFailureException("database down"));

        mockMvc.perform(get(DEPOSITS))
                .andExpect(status().isOk())
                .andExpect(header().doesNotExist("X-Data-Stale"));

        mockMvc.perform(get(DEPOSITS))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Data-Stale", "true"))
                .andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void returns503WhenDatabaseFailsAndNoResponseIsCached() throws Exception {
        when(getTimeDeposits.getTimeDeposits(anyInt(), anyInt()))
                .thenThrow(new DataAccessResourceFailureException("database down"));

        mockMvc.perform(get(DEPOSITS).param("limit", "2"))
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    void testUpdateBalances_whenRateLimitExceeded_thenTooManyRequests() throws Exception {
        when(updateAllBalances.updateAllBalances(any())).thenReturn(BalanceUpdateResult.APPLIED);

        mockMvc.perform(put(DEPOSITS + "/balances")).andExpect(status().isNoContent());
        mockMvc.perform(put(DEPOSITS + "/balances")).andExpect(status().isTooManyRequests());
    }
}
