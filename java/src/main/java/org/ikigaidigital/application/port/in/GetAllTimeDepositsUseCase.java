package org.ikigaidigital.application.port.in;

import java.util.List;

/**
 * Inbound port for the GET /time-deposits endpoint.
 */
public interface GetAllTimeDepositsUseCase {

    /** Every deposit, ordered by id (the response required by the assignment). */
    List<TimeDepositView> getAllTimeDeposits();

    /** Keyset page: up to {@code limit} deposits with id greater than {@code afterId}, ordered by id. */
    List<TimeDepositView> getTimeDeposits(int afterId, int limit);
}

