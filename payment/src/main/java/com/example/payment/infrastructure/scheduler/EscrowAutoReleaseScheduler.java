package com.example.payment.infrastructure.scheduler;

import com.example.payment.application.dto.EscrowReleaseCommand;
import com.example.payment.application.usecase.EscrowReleaseUseCase;
import com.example.payment.domain.entity.Escrow;
import com.example.payment.domain.enumtype.ConfirmationType;
import com.example.payment.domain.repository.EscrowRepository;
import com.example.payment.domain.service.TimeProvider;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
/**
 * releaseAt이 지난 escrow를 주기적으로 자동 구매확정 경로로 넘기는 scheduler다.
 * scheduler는 대상 조회와 command 생성만 담당하고, 실제 정산과 멱등 처리는 release usecase에 위임한다.
 */
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

    // todo: 배포에 대비하여 실행 시간을 조절하는 방법으로 변경이 필요
    @Scheduled(fixedDelayString = "${payment.escrow.auto-release.fixed-delay-ms:60000}")
    /**
     * 현재 시각 기준 자동 해제 대상 escrow를 조회해 AUTO confirmation으로 release를 요청한다.
     */
    public void releaseDueEscrows() {
        LocalDateTime now = timeProvider.now();
        List<Escrow> releaseTargets = escrowRepository.findReleaseTargets(now);

        for (Escrow escrow : releaseTargets) {
            escrowReleaseUseCase.releaseEscrow(
                    new EscrowReleaseCommand(
                            escrow.getOrderId(),
                            escrow.getSellerMemberId(),
                            ConfirmationType.AUTO
                    )
            );
        }
    }
}
