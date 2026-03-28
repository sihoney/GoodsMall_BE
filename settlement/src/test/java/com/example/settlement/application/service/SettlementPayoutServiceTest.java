package com.example.settlement.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.settlement.domain.entity.Settlement;
import com.example.settlement.domain.enumtype.SettlementStatus;
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
        Settlement pendingSettlement = Settlement.createPending(
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
                SettlementStatus.PENDING
        )).thenReturn(List.of(pendingSettlement));

        int requestedCount = settlementPayoutService.requestMonthlyPayouts(2026, 3);

        assertThat(requestedCount).isEqualTo(1);
        verify(payoutRequestedEventPublisher).publish(any());
    }

    @Test
    @DisplayName("SUCCESS 결과를 받으면 settlement 상태를 COMPLETED로 반영한다")
    void applyPayoutResult_success_completesSettlement() {
        UUID settlementId = UUID.randomUUID();
        Settlement settlement = Settlement.createPending(
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
        Settlement settlement = Settlement.createPending(
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
        Settlement settlement = Settlement.createPending(
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
        Settlement settlement = Settlement.createPending(
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
}
