package com.example.payment.presentation.controller;

import com.example.payment.application.dto.ChargeConfirmCommand;
import com.example.payment.application.dto.ChargeCreateCommand;
import com.example.payment.application.usecase.ChargeConfirmUseCase;
import com.example.payment.application.usecase.ChargeCreateUseCase;
import com.example.payment.domain.enumtype.PgProvider;
import com.example.payment.presentation.dto.request.ChargeConfirmRequest;
import com.example.payment.presentation.dto.request.ChargeCreateRequest;
import com.example.payment.presentation.dto.response.ChargeConfirmResponse;
import com.example.payment.presentation.dto.response.ChargeCreateResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payments/charges")
public class PaymentController {

	private final ChargeCreateUseCase chargeCreateUseCase;
	private final ChargeConfirmUseCase chargeConfirmUseCase;

	public PaymentController(
			ChargeCreateUseCase chargeCreateUseCase,
			ChargeConfirmUseCase chargeConfirmUseCase
	) {
		this.chargeCreateUseCase = chargeCreateUseCase;
		this.chargeConfirmUseCase = chargeConfirmUseCase;
	}

	@PostMapping
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
}
