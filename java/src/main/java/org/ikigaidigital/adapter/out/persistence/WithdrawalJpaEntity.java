package org.ikigaidigital.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "withdrawals")
public class WithdrawalJpaEntity {

    @Id
    @Column(name = "id")
    private Integer id;

    @Column(name = "timeDepositId", nullable = false)
    private Integer timeDepositId;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    protected WithdrawalJpaEntity() {
        // required by JPA
    }

    public WithdrawalJpaEntity(Integer id, Integer timeDepositId, BigDecimal amount, LocalDate date) {
        this.id = id;
        this.timeDepositId = timeDepositId;
        this.amount = amount;
        this.date = date;
    }

    public Integer getId() {
        return id;
    }

    public Integer getTimeDepositId() {
        return timeDepositId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public LocalDate getDate() {
        return date;
    }
}

