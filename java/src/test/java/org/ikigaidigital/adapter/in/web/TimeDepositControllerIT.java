package org.ikigaidigital.adapter.in.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end test exercising the two REST endpoints against the seeded
 * in-memory H2 database, confirming the full hexagonal wiring works.
 */
@SpringBootTest
@AutoConfigureMockMvc
class TimeDepositControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getAllTimeDeposits_returnsSeededDataWithWithdrawals() throws Exception {
        mockMvc.perform(get("/api/time-deposits/balances"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].planType").value("basic"))
                .andExpect(jsonPath("$[0].withdrawals[0].amount").value(500.0));
    }

    @Test
    void updateAllBalances_appliesInterestAndPersists() throws Exception {
        mockMvc.perform(put("/api/time-deposits/balances"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].balance").value(1235595.81)); // basic plan, 45 days, 1% annual
    }
}

