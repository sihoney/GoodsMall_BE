package com.example.payment.presentation.controller;

import com.example.payment.application.dto.ChargeConfirmCommand;
import com.example.payment.application.dto.ChargeCreateCommand;
import com.example.payment.application.dto.ChargeRefundCommand;
import com.example.payment.application.usecase.ChargeConfirmUseCase;
import com.example.payment.application.usecase.ChargeCreateUseCase;
import com.example.payment.application.usecase.ChargeRefundUseCase;
import com.example.payment.domain.enumtype.PgProvider;
import com.example.payment.presentation.dto.request.ChargeConfirmRequest;
import com.example.payment.presentation.dto.request.ChargeCreateRequest;
import com.example.payment.presentation.dto.request.ChargeRefundRequest;
import com.example.payment.presentation.dto.response.ChargeConfirmResponse;
import com.example.payment.presentation.dto.response.ChargeCreateResponse;
import com.example.payment.presentation.dto.response.ChargeRefundResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
/**
 * payment 충전 API 진입점이다.
 * HTTP 요청을 application command로 변환하고, usecase 결과를 presentation 응답 DTO로 매핑한다.
 */
public class PaymentController {

    private final ChargeCreateUseCase chargeCreateUseCase;
    private final ChargeConfirmUseCase chargeConfirmUseCase;
    private final ChargeRefundUseCase chargeRefundUseCase;

    public PaymentController(
            ChargeCreateUseCase chargeCreateUseCase,
            ChargeConfirmUseCase chargeConfirmUseCase,
            ChargeRefundUseCase chargeRefundUseCase
    ) {
        this.chargeCreateUseCase = chargeCreateUseCase;
        this.chargeConfirmUseCase = chargeConfirmUseCase;
        this.chargeRefundUseCase = chargeRefundUseCase;
    }

    /**
     * 충전 요청을 생성하고 PG 승인에 필요한 charge 식별 정보를 반환한다.
     */
    @PostMapping("/charge")
    public ChargeCreateResponse createCharge(
            @RequestHeader("X-Member-Id") UUID memberId,
            @Valid @RequestBody ChargeCreateRequest request
    ) {
        ChargeCreateCommand command = new ChargeCreateCommand(memberId, request.amount(), PgProvider.TOSS);
        return ChargeCreateResponse.from(chargeCreateUseCase.createCharge(command));
    }

    /**
     * PG 승인 결과를 받아 charge와 wallet 상태를 확정한다.
     */
    @PostMapping("/confirm")
    public ChargeConfirmResponse confirmCharge(@Valid @RequestBody ChargeConfirmRequest request) {
        ChargeConfirmCommand command = new ChargeConfirmCommand(
                request.chargeId(),
                request.paymentKey(),
                request.orderId(),
                request.amount()
        );
        return ChargeConfirmResponse.from(chargeConfirmUseCase.confirmCharge(command));
    }

    /**
     * 승인된 charge를 환불하고 wallet 잔액을 차감한다.
     */
    @PostMapping("/charges/{chargeId}/refund")
    public ChargeRefundResponse refundCharge(
            @PathVariable UUID chargeId,
            @Valid @RequestBody ChargeRefundRequest request
    ) {
        ChargeRefundCommand command = new ChargeRefundCommand(chargeId, request.refundReason());
        return ChargeRefundResponse.from(chargeRefundUseCase.refundCharge(command));
    }
}
