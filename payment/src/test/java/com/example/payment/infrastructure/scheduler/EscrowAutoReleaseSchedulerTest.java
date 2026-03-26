package com.example.payment.infrastructure.scheduler;

import com.example.payment.application.dto.EscrowReleaseCommand;
import com.example.payment.application.usecase.EscrowReleaseUseCase;
import com.example.payment.domain.entity.Escrow;
import com.example.payment.domain.enumtype.ConfirmationType;
import com.example.payment.domain.exception.EscrowAlreadyReleasedException;
import com.example.payment.domain.repository.EscrowRepository;
import com.example.payment.domain.service.TimeProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
@DisplayName("EscrowAutoReleaseScheduler 테스트")
class EscrowAutoReleaseSchedulerTest {

    @Mock
    private EscrowRepository escrowRepository;

    @Mock
    private EscrowReleaseUseCase escrowReleaseUseCase;

    @Mock
    private TimeProvider timeProvider;

    @InjectMocks
    private EscrowAutoReleaseScheduler scheduler;

    @Test
    @DisplayName("만기된 HELD escrow를 AUTO 기준으로 해제 요청한다")
    void releaseDueEscrows_callsReleaseUseCaseForTargets() {
        LocalDateTime now = LocalDateTime.of(2024, 1, 10, 10, 0, 0);
        UUID orderId = UUID.randomUUID();
        UUID buyerMemberId = UUID.randomUUID();
        UUID sellerMemberId = UUID.randomUUID();
        Escrow escrow = Escrow.createHeld(UUID.randomUUID(), orderId, buyerMemberId, sellerMemberId, 10_000L, now.minusMinutes(1), now.minusDays(7));

        when(timeProvider.now()).thenReturn(now);
        when(escrowRepository.findReleaseTargets(now)).thenReturn(List.of(escrow));

        scheduler.releaseDueEscrows();

        verify(escrowReleaseUseCase).releaseEscrow(
                new EscrowReleaseCommand(orderId, sellerMemberId, ConfirmationType.AUTO)
        );
    }

    @Test
    @DisplayName("이미 해제된 건으로 인한 중복 처리 예외는 무시한다")
    void releaseDueEscrows_ignoresAlreadyReleasedException() {
        LocalDateTime now = LocalDateTime.of(2024, 1, 10, 10, 0, 0);
        UUID orderId = UUID.randomUUID();
        UUID buyerMemberId = UUID.randomUUID();
        UUID sellerMemberId = UUID.randomUUID();
        Escrow escrow = Escrow.createHeld(UUID.randomUUID(), orderId, buyerMemberId, sellerMemberId, 10_000L, now.minusMinutes(1), now.minusDays(7));

        when(timeProvider.now()).thenReturn(now);
        when(escrowRepository.findReleaseTargets(now)).thenReturn(List.of(escrow));
        doThrow(new EscrowAlreadyReleasedException()).when(escrowReleaseUseCase)
                .releaseEscrow(new EscrowReleaseCommand(orderId, sellerMemberId, ConfirmationType.AUTO));

        scheduler.releaseDueEscrows();

        verify(escrowReleaseUseCase).releaseEscrow(
                new EscrowReleaseCommand(orderId, sellerMemberId, ConfirmationType.AUTO)
        );
    }
}
