package com.example.payment.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.example.payment.application.dto.EscrowReleaseCommand;
import com.example.payment.application.dto.EscrowReleaseResult;
import com.example.payment.common.exception.EscrowNotFoundException;
import com.example.payment.domain.entity.Escrow;
import com.example.payment.domain.enumtype.ConfirmationType;
import com.example.payment.domain.enumtype.EscrowStatus;
import com.example.payment.domain.repository.EscrowRepository;
import com.example.payment.domain.repository.EscrowTransactionRepository;
import com.example.payment.domain.service.IdentifierGenerator;
import com.example.payment.domain.service.SettlementCandidateCreatedEventPublisher;
import com.example.payment.domain.service.TimeProvider;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("EscrowReleaseService 테스트")
class EscrowReleaseServiceTest {

    @Mock
    private EscrowRepository escrowRepository;

    @Mock
    private EscrowTransactionRepository escrowTransactionRepository;

    @Mock
    private IdentifierGenerator identifierGenerator;

    @Mock
    private SettlementCandidateCreatedEventPublisher settlementCandidateCreatedEventPublisher;

    @Mock
    private TimeProvider timeProvider;

    @InjectMocks
    private EscrowReleaseService escrowReleaseService;

    private UUID orderId;
    private UUID buyerMemberId;
    private UUID sellerMemberId;
    private UUID escrowId;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        orderId = UUID.randomUUID();
        buyerMemberId = UUID.randomUUID();
        sellerMemberId = UUID.randomUUID();
        escrowId = UUID.randomUUID();
        now = LocalDateTime.of(2024, 1, 2, 10, 0, 0);
    }

    private BigDecimal amount(long value) {
        return BigDecimal.valueOf(value);
    }

    @Nested
    @DisplayName("releaseEscrow()")
    class ReleaseEscrow {

        @Test
        @DisplayName("정상 release 시 escrow를 RELEASED로 바꾸고 정산 후보 이벤트를 발행한다")
        void releaseEscrow_success_releasesEscrowAndPublishesSettlementCandidate() {
            EscrowReleaseCommand command = new EscrowReleaseCommand(orderId, sellerMemberId, ConfirmationType.MANUAL);
            Escrow escrow = Escrow.createHeld(
                    escrowId, orderId, buyerMemberId, sellerMemberId, amount(10_000L), now.minusDays(1)
            );

            given(escrowRepository.findAllByOrderIdAndSellerMemberId(orderId, sellerMemberId)).willReturn(List.of(escrow));
            given(timeProvider.now()).willReturn(now);
            given(identifierGenerator.generateUuid()).willReturn(UUID.randomUUID());
            given(escrowRepository.save(any(Escrow.class))).willAnswer(invocation -> invocation.getArgument(0));

            EscrowReleaseResult result = escrowReleaseService.releaseEscrow(command);

            assertThat(result.orderId()).isEqualTo(orderId);
            assertThat(result.releasedAmount()).isEqualTo(amount(10_000L));
            assertThat(result.escrowStatus()).isEqualTo(EscrowStatus.RELEASED);
            verify(escrowRepository).save(any(Escrow.class));
            verify(settlementCandidateCreatedEventPublisher).publish(any());
        }

        @Test
        @DisplayName("escrow가 없으면 EscrowNotFoundException을 던진다")
        void releaseEscrow_escrowNotFound_throwsException() {
            EscrowReleaseCommand command = new EscrowReleaseCommand(orderId, sellerMemberId, ConfirmationType.MANUAL);

            given(escrowRepository.findAllByOrderIdAndSellerMemberId(orderId, sellerMemberId)).willReturn(List.of());

            assertThatThrownBy(() -> escrowReleaseService.releaseEscrow(command))
                    .isInstanceOf(EscrowNotFoundException.class);
        }

        @Test
        @DisplayName("이미 RELEASED 상태면 기존 결과를 멱등하게 반환한다")
        void releaseEscrow_alreadyReleased_returnsExistingResult() {
            EscrowReleaseCommand command = new EscrowReleaseCommand(orderId, sellerMemberId, ConfirmationType.MANUAL);
            Escrow escrow = Escrow.createHeld(
                    escrowId, orderId, buyerMemberId, sellerMemberId, amount(10_000L), now.minusDays(1)
            );
            escrow.release(now.minusHours(1), now.minusHours(1));

            given(escrowRepository.findAllByOrderIdAndSellerMemberId(orderId, sellerMemberId)).willReturn(List.of(escrow));
            given(timeProvider.now()).willReturn(now);

            EscrowReleaseResult result = escrowReleaseService.releaseEscrow(command);

            assertThat(result.orderId()).isEqualTo(orderId);
            assertThat(result.releasedAmount()).isEqualTo(amount(10_000L));
            assertThat(result.escrowStatus()).isEqualTo(EscrowStatus.RELEASED);
            verify(settlementCandidateCreatedEventPublisher, never()).publish(any());
        }

        @Test
        @DisplayName("이미 REFUNDED 상태면 저장 없이 REFUNDED 결과를 반환한다")
        void releaseEscrow_refunded_returnsExistingResult() {
            EscrowReleaseCommand command = new EscrowReleaseCommand(orderId, sellerMemberId, ConfirmationType.MANUAL);
            Escrow escrow = Escrow.createHeld(
                    escrowId, orderId, buyerMemberId, sellerMemberId, amount(10_000L), now.minusDays(1)
            );
            escrow.applyRefundAmount(amount(10_000L), now.minusHours(1), now.minusHours(1));

            given(escrowRepository.findAllByOrderIdAndSellerMemberId(orderId, sellerMemberId)).willReturn(List.of(escrow));
            given(timeProvider.now()).willReturn(now);

            EscrowReleaseResult result = escrowReleaseService.releaseEscrow(command);

            assertThat(result.orderId()).isEqualTo(orderId);
            assertThat(result.releasedAmount()).isZero();
            assertThat(result.escrowStatus()).isEqualTo(EscrowStatus.REFUNDED);
            verify(escrowRepository, never()).save(any(Escrow.class));
            verify(settlementCandidateCreatedEventPublisher, never()).publish(any());
        }
    }
}
