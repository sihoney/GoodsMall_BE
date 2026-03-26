package com.example.payment.presentation.controller;

import com.example.payment.application.dto.ChargeConfirmCommand;
import com.example.payment.application.dto.ChargeCreateCommand;
import com.example.payment.application.dto.ChargeRefundCommand;
import com.example.payment.application.dto.EscrowReleaseScheduleCommand;
import com.example.payment.application.dto.EscrowReleaseCommand;
import com.example.payment.application.dto.OrderPaymentCommand;
import com.example.payment.application.usecase.ChargeConfirmUseCase;
import com.example.payment.application.usecase.ChargeCreateUseCase;
import com.example.payment.application.usecase.ChargeRefundUseCase;
import com.example.payment.application.usecase.EscrowReleaseUseCase;
import com.example.payment.application.usecase.EscrowReleaseScheduleUseCase;
import com.example.payment.application.usecase.OrderPaymentUseCase;
import com.example.payment.domain.enumtype.ConfirmationType;
import com.example.payment.domain.enumtype.PgProvider;
import com.example.payment.presentation.dto.request.ChargeConfirmRequest;
import com.example.payment.presentation.dto.request.ChargeCreateRequest;
import com.example.payment.presentation.dto.request.ChargeRefundRequest;
import com.example.payment.presentation.dto.request.EscrowReleaseScheduleRequest;
import com.example.payment.presentation.dto.request.ManualPurchaseConfirmRequest;
import com.example.payment.presentation.dto.request.OrderPaymentRequest;
import com.example.payment.presentation.dto.response.ChargeConfirmResponse;
import com.example.payment.presentation.dto.response.ChargeCreateResponse;
import com.example.payment.presentation.dto.response.ChargeRefundResponse;
import com.example.payment.presentation.dto.response.EscrowReleaseScheduleResponse;
import com.example.payment.presentation.dto.response.ManualPurchaseConfirmResponse;
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
	private final EscrowReleaseUseCase escrowReleaseUseCase;
	private final EscrowReleaseScheduleUseCase escrowReleaseScheduleUseCase;

	public PaymentController(
			ChargeCreateUseCase chargeCreateUseCase,
			ChargeConfirmUseCase chargeConfirmUseCase,
			ChargeRefundUseCase chargeRefundUseCase,
			OrderPaymentUseCase orderPaymentUseCase,
			EscrowReleaseUseCase escrowReleaseUseCase,
			EscrowReleaseScheduleUseCase escrowReleaseScheduleUseCase
	) {
		this.chargeCreateUseCase = chargeCreateUseCase;
		this.chargeConfirmUseCase = chargeConfirmUseCase;
		this.chargeRefundUseCase = chargeRefundUseCase;
		this.orderPaymentUseCase = orderPaymentUseCase;
		this.escrowReleaseUseCase = escrowReleaseUseCase;
		this.escrowReleaseScheduleUseCase = escrowReleaseScheduleUseCase;
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

	@PostMapping("/orders/{orderId}/delivery-complete")
	public EscrowReleaseScheduleResponse scheduleEscrowRelease(
			@PathVariable UUID orderId,
			@Valid @RequestBody EscrowReleaseScheduleRequest request
	) {
		EscrowReleaseScheduleCommand command = new EscrowReleaseScheduleCommand(
				orderId,
				request.deliveredAt()
		);
		return EscrowReleaseScheduleResponse.from(escrowReleaseScheduleUseCase.scheduleRelease(command));
	}

	@PostMapping("/orders/{orderId}/confirm")
	public ManualPurchaseConfirmResponse confirmPurchase(
			@PathVariable UUID orderId,
			@Valid @RequestBody ManualPurchaseConfirmRequest request
	) {
		EscrowReleaseCommand command = new EscrowReleaseCommand(
				orderId,
				request.sellerMemberId(),
				ConfirmationType.MANUAL
		);
		return ManualPurchaseConfirmResponse.from(escrowReleaseUseCase.releaseEscrow(command));
	}
}
