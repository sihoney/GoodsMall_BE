package com.example.payment.application.service;

import com.example.payment.application.dto.ChargeConfirmFailureCommand;
import com.example.payment.application.dto.ChargeConfirmFailureResult;
import com.example.payment.application.usecase.ChargeConfirmFailureUseCase;
import com.example.payment.common.exception.ChargeNotFoundException;
import com.example.payment.common.exception.InvalidChargeRequestException;
import com.example.payment.domain.entity.Charge;
import com.example.payment.domain.enumtype.ChargeStatus;
import com.example.payment.domain.repository.ChargeRepository;
import com.example.payment.domain.service.TimeProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ConfirmChargeFailureService implements ChargeConfirmFailureUseCase {

    private final ChargeRepository chargeRepository;
    private final TimeProvider timeProvider;

    public ConfirmChargeFailureService(
            ChargeRepository chargeRepository,
            TimeProvider timeProvider
    ) {
        this.chargeRepository = chargeRepository;
        this.timeProvider = timeProvider;
    }

    @Override
    public ChargeConfirmFailureResult confirmChargeFailure(ChargeConfirmFailureCommand command) {
        validateCommand(command);

        // pending 상태의 chargeId를 찾는다(orderId를 프론트에서 받기 때문)
        Charge charge = chargeRepository.findByPgOrderId(command.orderId())
                .orElseThrow(ChargeNotFoundException::new);

        // 상태가 pending이면 실패로 전환, pending이 아니면서 실패 상태가 아니면 예외
        // 이미 실패 상태인 경우는 멱등성 처리를 위해 기존 결과를 그대로 반환
        if (charge.isPending()) {
            charge.failAtRedirect(buildFailureReason(command.failureCode(), command.failureMessage()), timeProvider.now());
            chargeRepository.save(charge);
        } else if (!isFailed(charge)) {
            throw new IllegalStateException("Charge is not pending.");
        }

        return new ChargeConfirmFailureResult(
                charge.getChargeId(),
                charge.getChargeStatus(),
                charge.getPgOrderId(),
                charge.getFailureReason(),
                charge.getFailedAt()
        );
    }

    private void validateCommand(ChargeConfirmFailureCommand command) {
        if (command.orderId() == null || command.orderId().isBlank()) {
            throw new InvalidChargeRequestException("orderId is required.");
        }
        if (command.failureMessage() == null || command.failureMessage().isBlank()) {
            throw new InvalidChargeRequestException("message is required.");
        }
    }

    // 응답 코드랑 합쳐서 failreason을 만든다.
    private String buildFailureReason(String code, String message) {
        if (code == null || code.isBlank()) {
            return message;
        }
        return "[%s] %s".formatted(code, message);
    }

    // 실패 상태인지 점검
    private boolean isFailed(Charge charge) {
        return charge.getChargeStatus() == ChargeStatus.REDIRECT_FAILED;
    }
}
