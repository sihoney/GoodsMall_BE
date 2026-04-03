package com.example.payment.application.service;

import com.example.payment.application.dto.EscrowReleaseScheduleCommand;
import com.example.payment.application.dto.EscrowReleaseScheduleResult;
import com.example.payment.application.usecase.EscrowReleaseScheduleUseCase;
import com.example.payment.common.exception.EscrowNotFoundException;
import com.example.payment.common.exception.InvalidOrderPaymentRequestException;
import com.example.payment.domain.entity.Escrow;
import com.example.payment.domain.repository.EscrowRepository;
import com.example.payment.domain.service.TimeProvider;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
/**
 * 배송완료 이후 자동 구매확정 시점을 예약하는 유스케이스를 담당한다.
 * 주문에 속한 HELD escrow 전체에 같은 releaseAt을 설정한다.
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
     * 배송완료 시각을 기준으로 주문의 escrow 전체에 자동 구매확정 예약 시간을 설정한다.
     * 이미 예약되었거나 종료된 escrow는 그대로 두고 중복 이벤트를 흡수한다.
     */
    public EscrowReleaseScheduleResult scheduleRelease(EscrowReleaseScheduleCommand command) {
        validateCommand(command);

        // 배송완료는 주문 단위 이벤트이므로 해당 주문의 escrow 전체를 예약 대상으로 본다.
        List<Escrow> escrows = escrowRepository.findAllByOrderId(command.orderId());
        if (escrows.isEmpty()) {
            throw new EscrowNotFoundException();
        }

        LocalDateTime releaseAt = command.deliveredAt().plusDays(AUTO_CONFIRM_DAYS);
        LocalDateTime now = timeProvider.now();
        boolean updated = false;

        for (Escrow escrow : escrows) {
            // 이미 예약되었거나 종료된 escrow는 그대로 두고 중복 이벤트를 흡수한다.
            if (!escrow.isHeld() || escrow.getReleaseAt() != null) {
                continue;
            }
            escrow.scheduleReleaseAt(releaseAt, now);
            updated = true;
        }

        if (updated) {
            escrowRepository.saveAll(escrows);
        }

        return new EscrowReleaseScheduleResult(
                command.orderId(),
                command.deliveredAt(),
                releaseAt
        );
    }

    /**
     * 예약 처리에 필요한 최소 입력만 검증한다.
     */
    private void validateCommand(EscrowReleaseScheduleCommand command) {
        if (command == null) {
            throw new InvalidOrderPaymentRequestException("command is required.");
        }
        if (command.orderId() == null) {
            throw new InvalidOrderPaymentRequestException("orderId is required.");
        }
        if (command.deliveredAt() == null) {
            throw new InvalidOrderPaymentRequestException("deliveredAt is required.");
        }
    }
}
