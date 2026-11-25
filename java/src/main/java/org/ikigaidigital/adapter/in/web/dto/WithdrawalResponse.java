package org.ikigaidigital.adapter.in.web.dto;

import org.ikigaidigital.domain.Withdrawal;

import java.math.BigDecimal;
import java.time.LocalDate;

public record WithdrawalResponse(int id, BigDecimal amount, LocalDate date) {

    public static WithdrawalResponse from(Withdrawal withdrawal) {
        return new WithdrawalResponse(withdrawal.getId(), withdrawal.getAmount(), withdrawal.getDate());
    }
}

