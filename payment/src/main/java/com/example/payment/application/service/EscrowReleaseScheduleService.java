package com.example.payment.application.service;

import com.example.payment.application.dto.EscrowReleaseScheduleCommand;
import com.example.payment.application.dto.EscrowReleaseScheduleResult;
import com.example.payment.application.usecase.EscrowReleaseScheduleUseCase;
import com.example.payment.domain.entity.Escrow;
import com.example.payment.common.exception.EscrowNotFoundException;
import com.example.payment.common.exception.InvalidOrderPaymentRequestException;
import com.example.payment.domain.repository.EscrowRepository;
import com.example.payment.domain.service.TimeProvider;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
/**
 * 배송완료 이후 자동 구매확정 시점을 예약하는 유스케이스를 담당한다.
 * 실제 정산은 release 단계에서 수행하고, 이 서비스는 releaseAt 계산과 저장만 책임진다.
 */
public class EscrowReleaseScheduleService implements EscrowReleaseScheduleUseCase {

    private static final long AUTO_CONFIRM_DAYS = 7L;

    private final EscrowRepository escrowRepository;
    private final TimeProvider timeProvider;

    public EscrowReleaseScheduleService(EscrowRepository escrowRepository, TimeProvider timeProvider) {
        this.escrowRepository = escrowRepository;
        this.timeProvider = timeProvider;
    }

    @Override
    /**
     * HELD 상태이면서 아직 예약되지 않은 escrow에만 자동 구매확정 시점을 저장한다.
     * 이미 예약되었거나 종료된 escrow는 기존 값을 그대로 반환해 중복 이벤트를 흡수한다.
     */
    public EscrowReleaseScheduleResult scheduleRelease(EscrowReleaseScheduleCommand command) {
        validateCommand(command);

        Escrow escrow = escrowRepository.findByOrderId(command.orderId())
                .orElseThrow(EscrowNotFoundException::new);

        if (!escrow.isHeld() || escrow.getReleaseAt() != null) {
            return new EscrowReleaseScheduleResult(
                    escrow.getOrderId(),
                    command.deliveredAt(),
                    escrow.getReleaseAt()
            );
        }

        LocalDateTime releaseAt = command.deliveredAt().plusDays(AUTO_CONFIRM_DAYS);
        LocalDateTime now = timeProvider.now();

        escrow.scheduleReleaseAt(releaseAt, now);
        escrowRepository.save(escrow);

        return new EscrowReleaseScheduleResult(
                escrow.getOrderId(),
                command.deliveredAt(),
                escrow.getReleaseAt()
        );
    }

    /**
     * schedule 단계에서 필요한 최소 입력만 검증한다.
     * 실제 예약 가능 여부는 escrow 조회 이후 상태 기반으로 판단한다.
     */
    private void validateCommand(EscrowReleaseScheduleCommand command) {
        if (command.orderId() == null) {
            throw new InvalidOrderPaymentRequestException("orderId is required.");
        }
        if (command.deliveredAt() == null) {
            throw new InvalidOrderPaymentRequestException("deliveredAt is required.");
        }
    }
}
