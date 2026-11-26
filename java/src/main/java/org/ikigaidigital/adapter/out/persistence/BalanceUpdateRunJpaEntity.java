package org.ikigaidigital.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

@Entity
@Table(name = "balanceUpdateRuns")
public class BalanceUpdateRunJpaEntity {

    @Id
    @Column(name = "idempotencyKey")
    private String idempotencyKey;

    @Column(name = "executedAt", nullable = false)
    private OffsetDateTime executedAt;

    protected BalanceUpdateRunJpaEntity() {
    }

    public BalanceUpdateRunJpaEntity(String idempotencyKey, OffsetDateTime executedAt) {
        this.idempotencyKey = idempotencyKey;
        this.executedAt = executedAt;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public OffsetDateTime getExecutedAt() {
        return executedAt;
    }
}


