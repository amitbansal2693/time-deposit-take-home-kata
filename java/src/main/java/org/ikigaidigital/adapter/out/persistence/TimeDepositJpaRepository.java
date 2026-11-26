package org.ikigaidigital.adapter.out.persistence;

import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface TimeDepositJpaRepository extends JpaRepository<TimeDepositJpaEntity, Integer> {

    @Query("select d from TimeDepositJpaEntity d left join fetch d.withdrawals order by d.id")
    List<TimeDepositJpaEntity> findAllWithWithdrawalsOrderById();

    /** Ids first, so the page limit applies in SQL rather than in memory after the collection fetch join. */
    @Query("select d.id from TimeDepositJpaEntity d where d.id > :afterId order by d.id")
    List<Integer> findIdsAfter(@Param("afterId") int afterId, Pageable pageable);

    @Query("select distinct d from TimeDepositJpaEntity d left join fetch d.withdrawals where d.id in :ids order by d.id")
    List<TimeDepositJpaEntity> findWithWithdrawalsByIdIn(@Param("ids") Collection<Integer> ids);

    List<TimeDepositJpaEntity> findByIdGreaterThanOrderByIdAsc(Integer id, Limit limit);
}

