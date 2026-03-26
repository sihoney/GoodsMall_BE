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
public class EscrowReleaseScheduleService implements EscrowReleaseScheduleUseCase {

    private static final long AUTO_CONFIRM_DAYS = 7L;

    private final EscrowRepository escrowRepository;
    private final TimeProvider timeProvider;

    public EscrowReleaseScheduleService(EscrowRepository escrowRepository, TimeProvider timeProvider) {
        this.escrowRepository = escrowRepository;
        this.timeProvider = timeProvider;
    }

    @Override
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

    private void validateCommand(EscrowReleaseScheduleCommand command) {
        if (command.orderId() == null) {
            throw new InvalidOrderPaymentRequestException("orderId is required.");
        }
        if (command.deliveredAt() == null) {
            throw new InvalidOrderPaymentRequestException("deliveredAt is required.");
        }
    }
}
