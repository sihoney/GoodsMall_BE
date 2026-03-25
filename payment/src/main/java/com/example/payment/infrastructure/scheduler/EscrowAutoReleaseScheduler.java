package com.example.payment.infrastructure.scheduler;

import com.example.payment.application.dto.EscrowReleaseCommand;
import com.example.payment.application.usecase.EscrowReleaseUseCase;
import com.example.payment.domain.entity.Escrow;
import com.example.payment.domain.enumtype.ConfirmationType;
import com.example.payment.domain.exception.EscrowAlreadyReleasedException;
import com.example.payment.domain.repository.EscrowRepository;
import com.example.payment.domain.service.TimeProvider;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class EscrowAutoReleaseScheduler {

    private final EscrowRepository escrowRepository;
    private final EscrowReleaseUseCase escrowReleaseUseCase;
    private final TimeProvider timeProvider;

    public EscrowAutoReleaseScheduler(
            EscrowRepository escrowRepository,
            EscrowReleaseUseCase escrowReleaseUseCase,
            TimeProvider timeProvider
    ) {
        this.escrowRepository = escrowRepository;
        this.escrowReleaseUseCase = escrowReleaseUseCase;
        this.timeProvider = timeProvider;
    }

    @Scheduled(fixedDelayString = "${payment.escrow.auto-release.fixed-delay-ms:60000}")
    public void releaseDueEscrows() {
        LocalDateTime now = timeProvider.now();
        List<Escrow> releaseTargets = escrowRepository.findReleaseTargets(now);

        for (Escrow escrow : releaseTargets) {
            try {
                escrowReleaseUseCase.releaseEscrow(
                        new EscrowReleaseCommand(
                                escrow.getOrderId(),
                                escrow.getSellerMemberId(),
                                ConfirmationType.AUTO
                        )
                );
            } catch (EscrowAlreadyReleasedException e) {
                // Another process may have released it between query and execution.
            }
        }
    }
}
