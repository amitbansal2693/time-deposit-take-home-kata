package org.ikigaidigital.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.ikigaidigital.adapter.in.web.dto.TimeDepositResponse;
import org.ikigaidigital.application.port.in.GetAllTimeDepositsUseCase;
import org.ikigaidigital.application.port.in.UpdateAllTimeDepositBalancesUseCase;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Exposes exactly the two REST endpoints required by the assignment:
 *  - GET  /api/time-deposits/balances  -> retrieve all time deposits
 *  - PUT  /api/time-deposits/balances  -> recalculate and persist balances
 *
 * No other route is defined in this controller or anywhere else in the
 * application.
 */
@RestController
@RequestMapping("/api/time-deposits")
@Tag(name = "Time Deposits", description = "XA Bank time deposit operations")
public class TimeDepositController {

    private final GetAllTimeDepositsUseCase getAllTimeDepositsUseCase;
    private final UpdateAllTimeDepositBalancesUseCase updateAllTimeDepositBalancesUseCase;

    public TimeDepositController(GetAllTimeDepositsUseCase getAllTimeDepositsUseCase,
                                  UpdateAllTimeDepositBalancesUseCase updateAllTimeDepositBalancesUseCase) {
        this.getAllTimeDepositsUseCase = getAllTimeDepositsUseCase;
        this.updateAllTimeDepositBalancesUseCase = updateAllTimeDepositBalancesUseCase;
    }

    @GetMapping("/balances")
    @Operation(summary = "Retrieve all time deposits",
            description = "Returns every time deposit with id, planType, balance, days and withdrawals.")
    public List<TimeDepositResponse> getAllTimeDeposits() {
        return getAllTimeDepositsUseCase.getAllTimeDeposits().stream()
                .map(TimeDepositResponse::from)
                .toList();
    }

    @PutMapping("/balances")
    @Operation(summary = "Update all time deposit balances",
            description = "Applies one monthly interest calculation (per plan type) to every stored "
                    + "time deposit and persists the new balances. Returns the updated deposits.")
    public List<TimeDepositResponse> updateAllBalances() {
        updateAllTimeDepositBalancesUseCase.updateAllBalances();
        return getAllTimeDepositsUseCase.getAllTimeDeposits().stream()
                .map(TimeDepositResponse::from)
                .toList();
    }
}

