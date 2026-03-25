package com.example.payment.presentation.controller;

import com.example.payment.application.dto.ChargeConfirmCommand;
import com.example.payment.application.dto.ChargeCreateCommand;
import com.example.payment.application.dto.ChargeRefundCommand;
import com.example.payment.application.dto.OrderPaymentCommand;
import com.example.payment.application.usecase.ChargeConfirmUseCase;
import com.example.payment.application.usecase.ChargeCreateUseCase;
import com.example.payment.application.usecase.ChargeRefundUseCase;
import com.example.payment.application.usecase.OrderPaymentUseCase;
import com.example.payment.domain.enumtype.PgProvider;
import com.example.payment.presentation.dto.request.ChargeConfirmRequest;
import com.example.payment.presentation.dto.request.ChargeCreateRequest;
import com.example.payment.presentation.dto.request.ChargeRefundRequest;
import com.example.payment.presentation.dto.request.OrderPaymentRequest;
import com.example.payment.presentation.dto.response.ChargeConfirmResponse;
import com.example.payment.presentation.dto.response.ChargeCreateResponse;
import com.example.payment.presentation.dto.response.ChargeRefundResponse;
import com.example.payment.presentation.dto.response.OrderPaymentResponse;
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
public class PaymentController {

	private final ChargeCreateUseCase chargeCreateUseCase;
	private final ChargeConfirmUseCase chargeConfirmUseCase;
	private final ChargeRefundUseCase chargeRefundUseCase;
	private final OrderPaymentUseCase orderPaymentUseCase;

	public PaymentController(
			ChargeCreateUseCase chargeCreateUseCase,
			ChargeConfirmUseCase chargeConfirmUseCase,
			ChargeRefundUseCase chargeRefundUseCase,
			OrderPaymentUseCase orderPaymentUseCase
	) {
		this.chargeCreateUseCase = chargeCreateUseCase;
		this.chargeConfirmUseCase = chargeConfirmUseCase;
		this.chargeRefundUseCase = chargeRefundUseCase;
		this.orderPaymentUseCase = orderPaymentUseCase;
	}

	@PostMapping("/charge")
	public ChargeCreateResponse createCharge(
			@RequestHeader("X-Member-Id") UUID memberId,
			@Valid @RequestBody ChargeCreateRequest request
	) {
		ChargeCreateCommand command = new ChargeCreateCommand(memberId, request.amount(), PgProvider.TOSS);
		return ChargeCreateResponse.from(chargeCreateUseCase.createCharge(command));
	}

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

	@PostMapping("/charges/{chargeId}/refund")
	public ChargeRefundResponse refundCharge(
			@PathVariable UUID chargeId,
			@Valid @RequestBody ChargeRefundRequest request
	) {
		ChargeRefundCommand command = new ChargeRefundCommand(chargeId, request.refundReason());
		return ChargeRefundResponse.from(chargeRefundUseCase.refundCharge(command));
	}

	@PostMapping("/orders/pay")
	public OrderPaymentResponse payOrder(@Valid @RequestBody OrderPaymentRequest request) {
		OrderPaymentCommand command = new OrderPaymentCommand(
				request.orderId(),
				request.buyerMemberId(),
				request.sellerMemberId(),
				request.orderAmount(),
				request.sellerReceivableAmount(),
				request.releaseAt()
		);
		return OrderPaymentResponse.from(orderPaymentUseCase.payOrder(command));
	}
}
