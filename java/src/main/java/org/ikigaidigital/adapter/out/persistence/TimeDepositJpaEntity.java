package org.ikigaidigital.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "timeDeposits")
public class TimeDepositJpaEntity {

    @Id
    @Column(name = "id")
    private Integer id;

    @Column(name = "planType", nullable = false)
    private String planType;

    @Column(name = "days", nullable = false)
    private Integer days;

    @Column(name = "balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    protected TimeDepositJpaEntity() {
        // required by JPA
    }

    public TimeDepositJpaEntity(Integer id, String planType, Integer days, BigDecimal balance) {
        this.id = id;
        this.planType = planType;
        this.days = days;
        this.balance = balance;
    }

    public Integer getId() {
        return id;
    }

    public String getPlanType() {
        return planType;
    }

    public Integer getDays() {
        return days;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
}

