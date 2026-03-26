package com.example.payment.application.service;

import com.example.payment.application.dto.EscrowReleaseScheduleCommand;
import com.example.payment.application.dto.EscrowReleaseScheduleResult;
import com.example.payment.domain.entity.Escrow;
import com.example.payment.domain.exception.EscrowNotFoundException;
import com.example.payment.domain.exception.EscrowReleaseAlreadyScheduledException;
import com.example.payment.domain.exception.InvalidOrderPaymentRequestException;
import com.example.payment.domain.repository.EscrowRepository;
import com.example.payment.domain.service.TimeProvider;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("EscrowReleaseScheduleService 테스트")
class EscrowReleaseScheduleServiceTest {

    @Mock
    private EscrowRepository escrowRepository;

    @Mock
    private TimeProvider timeProvider;

    @InjectMocks
    private EscrowReleaseScheduleService escrowReleaseScheduleService;

    private UUID escrowId;
    private UUID orderId;
    private UUID buyerMemberId;
    private UUID sellerMemberId;
    private LocalDateTime deliveredAt;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        escrowId = UUID.randomUUID();
        orderId = UUID.randomUUID();
        buyerMemberId = UUID.randomUUID();
        sellerMemberId = UUID.randomUUID();
        deliveredAt = LocalDateTime.of(2024, 1, 10, 15, 0, 0);
        now = LocalDateTime.of(2024, 1, 10, 16, 0, 0);
    }

    @Nested
    @DisplayName("scheduleRelease() 테스트")
    class ScheduleRelease {

        @Test
        @DisplayName("배송 완료 시각 기준으로 7일 뒤 releaseAt을 설정한다")
        void scheduleRelease_success_setsReleaseAt() {
            EscrowReleaseScheduleCommand command = new EscrowReleaseScheduleCommand(orderId, deliveredAt);
            Escrow escrow = Escrow.createHeld(escrowId, orderId, buyerMemberId, sellerMemberId, 10_000L, null, now.minusDays(1));

            given(escrowRepository.findByOrderId(orderId)).willReturn(Optional.of(escrow));
            given(timeProvider.now()).willReturn(now);
            given(escrowRepository.save(any(Escrow.class))).willAnswer(inv -> inv.getArgument(0));

            EscrowReleaseScheduleResult result = escrowReleaseScheduleService.scheduleRelease(command);

            assertThat(result.orderId()).isEqualTo(orderId);
            assertThat(result.deliveredAt()).isEqualTo(deliveredAt);
            assertThat(result.releaseAt()).isEqualTo(deliveredAt.plusDays(7));
            verify(escrowRepository).save(any(Escrow.class));
        }

        @Test
        @DisplayName("에스크로가 없으면 예외가 발생한다")
        void scheduleRelease_escrowNotFound_throwsException() {
            EscrowReleaseScheduleCommand command = new EscrowReleaseScheduleCommand(orderId, deliveredAt);

            given(escrowRepository.findByOrderId(orderId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> escrowReleaseScheduleService.scheduleRelease(command))
                    .isInstanceOf(EscrowNotFoundException.class);
        }

        @Test
        @DisplayName("deliveredAt이 없으면 예외가 발생한다")
        void scheduleRelease_missingDeliveredAt_throwsException() {
            EscrowReleaseScheduleCommand command = new EscrowReleaseScheduleCommand(orderId, null);

            assertThatThrownBy(() -> escrowReleaseScheduleService.scheduleRelease(command))
                    .isInstanceOf(InvalidOrderPaymentRequestException.class)
                    .hasMessageContaining("deliveredAt is required.");

            verify(escrowRepository, never()).findByOrderId(any());
        }

        @Test
        @DisplayName("이미 releaseAt이 설정된 에스크로는 다시 설정할 수 없다")
        void scheduleRelease_alreadyScheduled_throwsException() {
            EscrowReleaseScheduleCommand command = new EscrowReleaseScheduleCommand(orderId, deliveredAt);
            Escrow escrow = Escrow.createHeld(escrowId, orderId, buyerMemberId, sellerMemberId, 10_000L, null, now.minusDays(1));
            escrow.scheduleReleaseAt(deliveredAt.plusDays(7), now.minusHours(1));

            given(escrowRepository.findByOrderId(orderId)).willReturn(Optional.of(escrow));
            given(timeProvider.now()).willReturn(now);

            assertThatThrownBy(() -> escrowReleaseScheduleService.scheduleRelease(command))
                    .isInstanceOf(EscrowReleaseAlreadyScheduledException.class)
                    .hasMessageContaining("already been scheduled");
        }
    }
}
