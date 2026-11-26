package org.ikigaidigital.application.service;

import org.ikigaidigital.TimeDeposit;
import org.ikigaidigital.TimeDepositCalculator;
import org.ikigaidigital.application.port.in.BalanceUpdateResult;
import org.ikigaidigital.application.port.in.TimeDepositView;
import org.ikigaidigital.application.port.out.BalanceUpdateRunPort;
import org.ikigaidigital.application.port.out.TimeDepositRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TimeDepositApplicationServiceTest {

    private static final int BATCH_SIZE = 2;

    @Mock
    private TimeDepositRepositoryPort repository;

    @Mock
    private BalanceUpdateRunPort updateRuns;

    private final TimeDepositCalculator calculator = new TimeDepositCalculator();
    private TimeDepositApplicationService service;

    @BeforeEach
    void setUp() {
        service = new TimeDepositApplicationService(repository, updateRuns, calculator, BATCH_SIZE);
    }

    @Test
    void updateAllBalances_whenMultipleBatches_thenEachBatchProcessedOnceUnderTheLock() {
        List<TimeDeposit> deposits = List.of(
                new TimeDeposit(1, "basic", 1200.0, 45),
                new TimeDeposit(2, "student", 1200.0, 100),
                new TimeDeposit(3, "premium", 1200.0, 90),
                new TimeDeposit(4, "basic", 1200.0, 10),
                new TimeDeposit(5, "premium", 1200.0, 46));
        stubKeyset(deposits);

        BalanceUpdateResult result = service.updateAllBalances();

        assertThat(result).isEqualTo(BalanceUpdateResult.APPLIED);
        verify(updateRuns).acquireExclusiveLock();
        // Keyset progression: batches of 2, 2, 1, then an empty batch that stops the loop.
        verify(repository).findBatchAfterId(Integer.MIN_VALUE, BATCH_SIZE);
        verify(repository).findBatchAfterId(2, BATCH_SIZE);
        verify(repository).findBatchAfterId(4, BATCH_SIZE);
        verify(repository).findBatchAfterId(5, BATCH_SIZE);
        verify(repository, times(3)).saveBalances(anyList());
        assertThat(deposits).extracting(TimeDeposit::getBalance)
                .containsExactly(1201.0, 1203.0, 1205.0, 1200.0, 1205.0);
    }

    @Test
    void updateAllBalances_whenSameIdempotencyKeyTwice_thenInterestAppliedOnce() {
        TimeDeposit deposit = new TimeDeposit(1, "basic", 1200.0, 45);
        stubKeyset(List.of(deposit));
        when(updateRuns.isAlreadyApplied("2026-11")).thenReturn(false, true);

        assertThat(service.updateAllBalances("2026-11")).isEqualTo(BalanceUpdateResult.APPLIED);
        assertThat(service.updateAllBalances("2026-11")).isEqualTo(BalanceUpdateResult.ALREADY_APPLIED);

        verify(updateRuns).recordApplied("2026-11");
        assertThat(deposit.getBalance()).isEqualTo(1201.0);
    }

    @Test
    void updateAllBalances_whenCalledWithoutKeyTwice_thenInterestAppliedEachTime() {
        TimeDeposit deposit = new TimeDeposit(1, "basic", 1200.0, 45);
        stubKeyset(List.of(deposit));

        service.updateAllBalances();
        service.updateAllBalances();

        assertThat(deposit.getBalance()).isEqualTo(1202.0);
        verify(updateRuns, never()).isAlreadyApplied(any());
        verify(updateRuns, never()).recordApplied(any());
    }

    @Test
    void getAllTimeDeposits_whenCalled_thenReturnsRepositoryResult() {
        List<TimeDepositView> views = List.of(
                new TimeDepositView(new TimeDeposit(1, "basic", 1000.0, 30), List.of()));
        when(repository.findAllWithWithdrawals()).thenReturn(views);

        assertThat(service.getAllTimeDeposits()).isEqualTo(views);
    }

    @Test
    void getTimeDeposits_whenPageRequested_thenQueriesRepositoryPage() {
        List<TimeDepositView> page = List.of(
                new TimeDepositView(new TimeDeposit(5, "premium", 1000.0, 90), List.of()));
        when(repository.findPageWithWithdrawals(4, 10)).thenReturn(page);

        assertThat(service.getTimeDeposits(4, 10)).isEqualTo(page);
    }

    /** Stubs the keyset reader to return deposits with id greater than lastId, up to the batch size. */
    private void stubKeyset(List<TimeDeposit> all) {
        when(repository.findBatchAfterId(anyInt(), anyInt())).thenAnswer(invocation -> {
            int lastId = invocation.getArgument(0);
            int size = invocation.getArgument(1);
            return all.stream().filter(d -> d.getId() > lastId).limit(size).toList();
        });
    }
}


