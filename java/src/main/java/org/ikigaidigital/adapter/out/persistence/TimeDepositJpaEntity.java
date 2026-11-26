package org.ikigaidigital.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "timeDeposits")
public class TimeDepositJpaEntity {

    @Id
    private Integer id;

    @Column(name = "planType", nullable = false)
    private String planType;

    @Column(nullable = false)
    private Integer days;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    // Read-only association: lazy by default, fetched explicitly only for the GET query.
    @OneToMany
    @JoinColumn(name = "timeDepositId", insertable = false, updatable = false)
    @OrderBy("id ASC")
    private List<WithdrawalJpaEntity> withdrawals = new ArrayList<>();

    protected TimeDepositJpaEntity() {
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

    public List<WithdrawalJpaEntity> getWithdrawals() {
        return withdrawals;
    }
}

