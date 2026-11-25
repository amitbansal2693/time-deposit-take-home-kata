package org.ikigaidigital.application.port.in;

import java.util.List;

/**
 * Inbound port for the GET /time-deposits endpoint.
 */
public interface GetAllTimeDepositsUseCase {

    List<TimeDepositView> getAllTimeDeposits();
}

