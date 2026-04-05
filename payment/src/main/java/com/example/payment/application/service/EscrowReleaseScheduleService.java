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
    /** todo:
     *   배송완료 시각을 기준으로 escrow의 자동 구매확정 예약 시간을 설정한다.
     *   현재는 주문 단위 이벤트를 받아 orderId로 주문의 escrow 전체를 조회한 뒤 예약한다.
     *   추후 부분 배송/부분 취소를 지원하면 orderItemId 단위 이벤트로 변경하고,
     *   해당 orderItem에 매핑되는 escrow 1건(escrowId)을 조회해 상태 전이하도록 개선할 수 있다.
     *   이렇게 하면 주문 전체 조회/순회를 줄이고, 변경 대상 escrow만 정밀하게 처리할 수 있다.
     */
    public EscrowReleaseScheduleResult scheduleRelease(EscrowReleaseScheduleCommand command) {
        validateCommand(command);

        // 배송완료는 주문 단위 이벤트이므로 해당 주문의 escrow 전체를 예약 대상으로 본다.
        //todo: 배송 완료를 주문 단위가 아닌 배송 아이템 단위로 변경하여 로직을 수정할 것.
        List<Escrow> escrows = escrowRepository.findAllByOrderId(command.orderId());
        if (escrows.isEmpty()) {
            throw new EscrowNotFoundException();
        }

        LocalDateTime releaseAt = command.deliveredAt().plusDays(AUTO_CONFIRM_DAYS);
        LocalDateTime now = timeProvider.now();
        // 실제로 변경된 scrow가 하나라도 있는지 확인하기 위한 변수
        boolean updated = false;

        for (Escrow escrow : escrows) {
            // 이미 예약되었거나 종료된 escrow는 그대로 두고 중복 이벤트를 흡수한다.
            if (!escrow.isHeld() || escrow.getReleaseAt() != null) {
                continue;
            }
            escrow.scheduleReleaseAt(releaseAt, now);
            updated = true;
        }
        // 변경된 escrow가 있을 때만 저장하여 중복 이벤트 재처리 시 불필요한 save를 피한다.
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
