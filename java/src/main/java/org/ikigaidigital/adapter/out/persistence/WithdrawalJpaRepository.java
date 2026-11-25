package org.ikigaidigital.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface WithdrawalJpaRepository extends JpaRepository<WithdrawalJpaEntity, Integer> {

    // Single set-oriented query to avoid N+1 lookups when assembling the GET response.
    List<WithdrawalJpaEntity> findByTimeDepositIdIn(Collection<Integer> timeDepositIds);
}

