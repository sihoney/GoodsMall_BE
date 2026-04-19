package com.example.settlement.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.settlement.domain.entity.Settlement;
import com.example.settlement.domain.enumtype.SettlementStatus;
import com.example.settlement.domain.enumtype.SettlementType;
import com.example.settlement.domain.repository.SettlementRepository;
import com.example.settlement.infrastructure.messaging.kafka.KafkaSellerSettlementPayoutRequestedEventPublisher;
import com.example.settlement.infrastructure.messaging.kafka.contract.PayoutFailureReason;
import com.example.settlement.infrastructure.messaging.kafka.contract.SellerSettlementPayoutResultMessage;
import com.example.settlement.infrastructure.messaging.kafka.contract.SellerSettlementPayoutResultStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("SettlementPayoutService 테스트")
class SettlementPayoutServiceTest {

    @Mock
    private SettlementRepository settlementRepository;

    @Mock
    private KafkaSellerSettlementPayoutRequestedEventPublisher payoutRequestedEventPublisher;

    private SettlementPayoutService settlementPayoutService;

    @BeforeEach
    void setUp() {
        settlementPayoutService = new SettlementPayoutService(settlementRepository, payoutRequestedEventPublisher);
    }

    @Test
    @DisplayName("월별 PENDING 정산건 수만큼 지급 요청 이벤트를 발행한다")
    void requestMonthlyPayouts_publishesEventsForPendingSettlements() {
        Settlement pendingSettlement = Settlement.createMonthlyPending(
                UUID.randomUUID(),
                UUID.randomUUID(),
                2026,
                3,
                10_000L,
                1_000L,
                9_000L,
                LocalDateTime.of(2026, 4, 1, 3, 5)
        );
        when(settlementRepository.findBySettlementYearAndSettlementMonthAndSettlementStatus(
                2026,
                3,
                SettlementStatus.PENDING,
                SettlementType.MONTHLY
        )).thenReturn(List.of(pendingSettlement));

        int requestedCount = settlementPayoutService.requestMonthlyPayouts(2026, 3);

        assertThat(requestedCount).isEqualTo(1);
        verify(payoutRequestedEventPublisher).publish(any());
    }

    @Test
    @DisplayName("SUCCESS 결과를 받으면 settlement 상태를 COMPLETED로 반영한다")
    void applyPayoutResult_success_completesSettlement() {
        UUID settlementId = UUID.randomUUID();
        Settlement settlement = Settlement.createMonthlyPending(
                settlementId,
                UUID.randomUUID(),
                2026,
                3,
                10_000L,
                1_000L,
                9_000L,
                LocalDateTime.of(2026, 4, 1, 3, 5)
        );
        when(settlementRepository.findBySettlementId(settlementId)).thenReturn(Optional.of(settlement));

        settlementPayoutService.applyPayoutResult(new SellerSettlementPayoutResultMessage(
                UUID.randomUUID(),
                UUID.randomUUID(),
                settlementId,
                settlement.getSellerId(),
                9_000L,
                SellerSettlementPayoutResultStatus.SUCCESS,
                null,
                LocalDateTime.of(2026, 4, 1, 3, 10)
        ));

        assertThat(settlement.getSettlementStatus()).isEqualTo(SettlementStatus.COMPLETED);
        verify(settlementRepository).save(settlement);
    }

    @Test
    @DisplayName("이미 COMPLETED인 settlement에 SUCCESS 중복 이벤트가 와도 no-op 처리한다")
    void applyPayoutResult_duplicateSuccessOnCompleted_isNoOp() {
        UUID settlementId = UUID.randomUUID();
        Settlement settlement = Settlement.create(
                settlementId,
                UUID.randomUUID(),
                SettlementType.MONTHLY,
                2026,
                3,
                10_000L,
                1_000L,
                9_000L,
                9_000L,
                SettlementStatus.COMPLETED,
                LocalDateTime.of(2026, 4, 1, 3, 10),
                null,
                LocalDateTime.of(2026, 4, 1, 3, 5),
                LocalDateTime.of(2026, 4, 1, 3, 10)
        );
        when(settlementRepository.findBySettlementId(settlementId)).thenReturn(Optional.of(settlement));

        settlementPayoutService.applyPayoutResult(new SellerSettlementPayoutResultMessage(
                UUID.randomUUID(),
                UUID.randomUUID(),
                settlementId,
                settlement.getSellerId(),
                9_000L,
                SellerSettlementPayoutResultStatus.SUCCESS,
                null,
                LocalDateTime.of(2026, 4, 1, 3, 11)
        ));

        verify(settlementRepository, never()).save(any());
    }

    @Test
    @DisplayName("NON_RETRYABLE 실패 결과를 받으면 settlement 상태를 FAILED로 반영하고 failureReason을 저장한다")
    void applyPayoutResult_nonRetryableFailure_failsSettlementWithReason() {
        UUID settlementId = UUID.randomUUID();
        Settlement settlement = Settlement.createMonthlyPending(
                settlementId,
                UUID.randomUUID(),
                2026,
                3,
                10_000L,
                1_000L,
                9_000L,
                LocalDateTime.of(2026, 4, 1, 3, 5)
        );
        when(settlementRepository.findBySettlementId(settlementId)).thenReturn(Optional.of(settlement));

        settlementPayoutService.applyPayoutResult(new SellerSettlementPayoutResultMessage(
                UUID.randomUUID(),
                UUID.randomUUID(),
                settlementId,
                settlement.getSellerId(),
                9_000L,
                SellerSettlementPayoutResultStatus.FAILED,
                PayoutFailureReason.WALLET_NOT_FOUND,
                LocalDateTime.of(2026, 4, 1, 3, 10)
        ));

        assertThat(settlement.getSettlementStatus()).isEqualTo(SettlementStatus.FAILED);
        assertThat(settlement.getLastFailureReason()).isEqualTo(PayoutFailureReason.WALLET_NOT_FOUND.name());
        verify(settlementRepository).save(settlement);
    }

    @Test
    @DisplayName("RETRYABLE 실패 결과를 받으면 settlement 상태를 FAILED로 반영하고 failureReason을 저장한다")
    void applyPayoutResult_retryableFailure_failsSettlementWithReason() {
        UUID settlementId = UUID.randomUUID();
        Settlement settlement = Settlement.createMonthlyPending(
                settlementId,
                UUID.randomUUID(),
                2026,
                3,
                10_000L,
                1_000L,
                9_000L,
                LocalDateTime.of(2026, 4, 1, 3, 5)
        );
        when(settlementRepository.findBySettlementId(settlementId)).thenReturn(Optional.of(settlement));

        settlementPayoutService.applyPayoutResult(new SellerSettlementPayoutResultMessage(
                UUID.randomUUID(),
                UUID.randomUUID(),
                settlementId,
                settlement.getSellerId(),
                9_000L,
                SellerSettlementPayoutResultStatus.FAILED,
                PayoutFailureReason.INTERNAL_ERROR,
                LocalDateTime.of(2026, 4, 1, 3, 10)
        ));

        assertThat(settlement.getSettlementStatus()).isEqualTo(SettlementStatus.FAILED);
        assertThat(settlement.getLastFailureReason()).isEqualTo(PayoutFailureReason.INTERNAL_ERROR.name());
        verify(settlementRepository).save(settlement);
    }

    @Test
    @DisplayName("failureReason 없이 FAILED 결과를 받으면 INTERNAL_ERROR로 처리한다")
    void applyPayoutResult_failedWithNullReason_usesInternalError() {
        UUID settlementId = UUID.randomUUID();
        Settlement settlement = Settlement.createMonthlyPending(
                settlementId,
                UUID.randomUUID(),
                2026,
                3,
                10_000L,
                1_000L,
                9_000L,
                LocalDateTime.of(2026, 4, 1, 3, 5)
        );
        when(settlementRepository.findBySettlementId(settlementId)).thenReturn(Optional.of(settlement));

        settlementPayoutService.applyPayoutResult(new SellerSettlementPayoutResultMessage(
                UUID.randomUUID(),
                UUID.randomUUID(),
                settlementId,
                settlement.getSellerId(),
                9_000L,
                SellerSettlementPayoutResultStatus.FAILED,
                null,
                LocalDateTime.of(2026, 4, 1, 3, 10)
        ));

        assertThat(settlement.getSettlementStatus()).isEqualTo(SettlementStatus.FAILED);
        assertThat(settlement.getLastFailureReason()).isEqualTo(PayoutFailureReason.INTERNAL_ERROR.name());
        verify(settlementRepository).save(settlement);
    }

    @Test
    @DisplayName("RETRYABLE 실패 정산건은 PENDING으로 복구 후 즉시 payout 재요청되어 PROCESSING이 된다")
    void requestRetryableFailedPayouts_retryableFailure_requeuesAndPublishes() {
        UUID settlementId = UUID.randomUUID();
        Settlement failedSettlement = Settlement.create(
                settlementId,
                UUID.randomUUID(),
                SettlementType.MONTHLY,
                2026,
                3,
                10_000L,
                1_000L,
                9_000L,
                0L,
                SettlementStatus.FAILED,
                null,
                PayoutFailureReason.INTERNAL_ERROR.name(),
                LocalDateTime.of(2026, 4, 1, 3, 5),
                LocalDateTime.of(2026, 4, 1, 3, 10)
        );
        when(settlementRepository.findBySettlementYearAndSettlementMonthAndSettlementStatus(
                2026,
                3,
                SettlementStatus.FAILED,
                SettlementType.MONTHLY
        )).thenReturn(List.of(failedSettlement));

        int retriedCount = settlementPayoutService.requestRetryableFailedPayouts(2026, 3);

        assertThat(retriedCount).isEqualTo(1);
        assertThat(failedSettlement.getSettlementStatus()).isEqualTo(SettlementStatus.PROCESSING);
        assertThat(failedSettlement.getLastFailureReason()).isNull();
        verify(settlementRepository, times(2)).save(failedSettlement);
        verify(payoutRequestedEventPublisher).publish(any());
    }

    @Test
    @DisplayName("NON_RETRYABLE 실패 정산건은 재지급 요청에서 제외한다")
    void requestRetryableFailedPayouts_nonRetryableFailure_skipsRetry() {
        Settlement failedSettlement = Settlement.create(
                UUID.randomUUID(),
                UUID.randomUUID(),
                SettlementType.MONTHLY,
                2026,
                3,
                10_000L,
                1_000L,
                9_000L,
                0L,
                SettlementStatus.FAILED,
                null,
                PayoutFailureReason.WALLET_NOT_FOUND.name(),
                LocalDateTime.of(2026, 4, 1, 3, 5),
                LocalDateTime.of(2026, 4, 1, 3, 10)
        );
        when(settlementRepository.findBySettlementYearAndSettlementMonthAndSettlementStatus(
                2026,
                3,
                SettlementStatus.FAILED,
                SettlementType.MONTHLY
        )).thenReturn(List.of(failedSettlement));

        int retriedCount = settlementPayoutService.requestRetryableFailedPayouts(2026, 3);

        assertThat(retriedCount).isZero();
        assertThat(failedSettlement.getSettlementStatus()).isEqualTo(SettlementStatus.FAILED);
        verify(settlementRepository, never()).save(failedSettlement);
        verify(payoutRequestedEventPublisher, never()).publish(any());
    }

    @Test
    @DisplayName("NON_RETRYABLE 실패 정산건은 수동 재지급 요청 후 즉시 payout 재요청되어 PROCESSING이 된다")
    void requestManualFailedPayout_nonRetryableFailure_allowsManualRetry() {
        UUID settlementId = UUID.randomUUID();
        Settlement failedSettlement = Settlement.create(
                settlementId,
                UUID.randomUUID(),
                SettlementType.MONTHLY,
                2026,
                3,
                10_000L,
                1_000L,
                9_000L,
                0L,
                SettlementStatus.FAILED,
                null,
                PayoutFailureReason.WALLET_NOT_FOUND.name(),
                LocalDateTime.of(2026, 4, 1, 3, 5),
                LocalDateTime.of(2026, 4, 1, 3, 10)
        );
        when(settlementRepository.findBySettlementId(settlementId)).thenReturn(Optional.of(failedSettlement));

        boolean requested = settlementPayoutService.requestManualFailedPayout(settlementId);

        assertThat(requested).isTrue();
        assertThat(failedSettlement.getSettlementStatus()).isEqualTo(SettlementStatus.PROCESSING);
        assertThat(failedSettlement.getLastFailureReason()).isNull();
        verify(settlementRepository, times(2)).save(failedSettlement);
        verify(payoutRequestedEventPublisher).publish(any());
    }

    @Test
    @DisplayName("RETRYABLE 실패 정산건은 수동 재지급 요청을 차단한다")
    void requestManualFailedPayout_retryableFailure_skipsManualRetry() {
        UUID settlementId = UUID.randomUUID();
        Settlement failedSettlement = Settlement.create(
                settlementId,
                UUID.randomUUID(),
                SettlementType.MONTHLY,
                2026,
                3,
                10_000L,
                1_000L,
                9_000L,
                0L,
                SettlementStatus.FAILED,
                null,
                PayoutFailureReason.INTERNAL_ERROR.name(),
                LocalDateTime.of(2026, 4, 1, 3, 5),
                LocalDateTime.of(2026, 4, 1, 3, 10)
        );
        when(settlementRepository.findBySettlementId(settlementId)).thenReturn(Optional.of(failedSettlement));

        boolean requested = settlementPayoutService.requestManualFailedPayout(settlementId);

        assertThat(requested).isFalse();
        assertThat(failedSettlement.getSettlementStatus()).isEqualTo(SettlementStatus.FAILED);
        verify(settlementRepository, never()).save(any());
        verify(payoutRequestedEventPublisher, never()).publish(any());
    }

    @Test
    @DisplayName("DLQ replay 대상 목록을 처리할 때 RETRYABLE은 자동 재요청하고 나머지는 정책에 맞게 집계한다")
    void replayFailedPayouts_classifiesAndProcessesByReason() {
        UUID retryableId = UUID.randomUUID();
        UUID nonRetryableId = UUID.randomUUID();
        UUID notFailedStatusId = UUID.randomUUID();
        UUID notFoundId = UUID.randomUUID();

        Settlement retryableFailedSettlement = Settlement.create(
                retryableId,
                UUID.randomUUID(),
                SettlementType.MONTHLY,
                2026,
                3,
                10_000L,
                1_000L,
                9_000L,
                0L,
                SettlementStatus.FAILED,
                null,
                PayoutFailureReason.INTERNAL_ERROR.name(),
                LocalDateTime.of(2026, 4, 1, 3, 5),
                LocalDateTime.of(2026, 4, 1, 3, 10)
        );
        Settlement nonRetryableFailedSettlement = Settlement.create(
                nonRetryableId,
                UUID.randomUUID(),
                SettlementType.MONTHLY,
                2026,
                3,
                10_000L,
                1_000L,
                9_000L,
                0L,
                SettlementStatus.FAILED,
                null,
                PayoutFailureReason.WALLET_NOT_FOUND.name(),
                LocalDateTime.of(2026, 4, 1, 3, 5),
                LocalDateTime.of(2026, 4, 1, 3, 10)
        );
        Settlement completedSettlement = Settlement.create(
                notFailedStatusId,
                UUID.randomUUID(),
                SettlementType.MONTHLY,
                2026,
                3,
                10_000L,
                1_000L,
                9_000L,
                9_000L,
                SettlementStatus.COMPLETED,
                LocalDateTime.of(2026, 4, 1, 3, 10),
                null,
                LocalDateTime.of(2026, 4, 1, 3, 5),
                LocalDateTime.of(2026, 4, 1, 3, 10)
        );

        when(settlementRepository.findBySettlementId(retryableId)).thenReturn(Optional.of(retryableFailedSettlement));
        when(settlementRepository.findBySettlementId(nonRetryableId)).thenReturn(Optional.of(nonRetryableFailedSettlement));
        when(settlementRepository.findBySettlementId(notFailedStatusId)).thenReturn(Optional.of(completedSettlement));
        when(settlementRepository.findBySettlementId(notFoundId)).thenReturn(Optional.empty());

        java.util.List<UUID> replayTargets = new java.util.ArrayList<>();
        replayTargets.add(retryableId);
        replayTargets.add(nonRetryableId);
        replayTargets.add(notFailedStatusId);
        replayTargets.add(notFoundId);
        replayTargets.add(null);

        var result = settlementPayoutService.replayFailedPayouts(replayTargets);

        assertThat(result.requestedRetryCount()).isEqualTo(1);
        assertThat(result.manualActionRequiredCount()).isEqualTo(1);
        assertThat(result.skippedCount()).isEqualTo(2);
        assertThat(result.notFoundCount()).isEqualTo(1);

        assertThat(retryableFailedSettlement.getSettlementStatus()).isEqualTo(SettlementStatus.PROCESSING);
        verify(settlementRepository, times(2)).save(retryableFailedSettlement);
        verify(payoutRequestedEventPublisher).publish(any());
        verify(settlementRepository, never()).save(nonRetryableFailedSettlement);
    }

    @Test
    @DisplayName("DLQ replay 목록이 null이면 예외가 발생한다")
    void replayFailedPayouts_nullList_throwsException() {
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> settlementPayoutService.replayFailedPayouts(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("settlementIds");
    }

    @Test
    @DisplayName("DLQ replay에 같은 settlementId가 중복되면 1회만 처리한다")
    void replayFailedPayouts_duplicateSettlementId_processesOnce() {
        UUID duplicatedId = UUID.randomUUID();

        Settlement retryableFailedSettlement = Settlement.create(
                duplicatedId,
                UUID.randomUUID(),
                SettlementType.MONTHLY,
                2026,
                3,
                10_000L,
                1_000L,
                9_000L,
                0L,
                SettlementStatus.FAILED,
                null,
                PayoutFailureReason.INTERNAL_ERROR.name(),
                LocalDateTime.of(2026, 4, 1, 3, 5),
                LocalDateTime.of(2026, 4, 1, 3, 10)
        );
        when(settlementRepository.findBySettlementId(duplicatedId)).thenReturn(Optional.of(retryableFailedSettlement));

        var result = settlementPayoutService.replayFailedPayouts(List.of(duplicatedId, duplicatedId));

        assertThat(result.requestedRetryCount()).isEqualTo(1);
        assertThat(result.manualActionRequiredCount()).isZero();
        assertThat(result.skippedCount()).isZero();
        assertThat(result.notFoundCount()).isZero();
        verify(settlementRepository, times(1)).findBySettlementId(duplicatedId);
        verify(payoutRequestedEventPublisher, times(1)).publish(any());
    }
}
