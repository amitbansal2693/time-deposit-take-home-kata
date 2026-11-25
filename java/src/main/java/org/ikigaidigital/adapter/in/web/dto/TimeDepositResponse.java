package org.ikigaidigital.adapter.in.web.dto;

import org.ikigaidigital.application.port.in.TimeDepositView;

import java.util.List;

/**
 * Response shape for GET /api/time-deposits, matching the schema required
 * by the assignment: id, planType, balance, days, withdrawals.
 */
public record TimeDepositResponse(
        int id,
        String planType,
        double balance,
        int days,
        List<WithdrawalResponse> withdrawals) {

    public static TimeDepositResponse from(TimeDepositView view) {
        return new TimeDepositResponse(
                view.timeDeposit().getId(),
                view.timeDeposit().getPlanType(),
                view.timeDeposit().getBalance(),
                view.timeDeposit().getDays(),
                view.withdrawals().stream().map(WithdrawalResponse::from).toList());
    }
}

