package com.example.payment.application.service;

import com.example.payment.application.dto.OrderPaymentValidationCommand;
import com.example.payment.application.usecase.OrderPaymentValidationUseCase;
import com.example.payment.common.exception.InvalidCardPaymentRequestException;
import com.example.payment.domain.service.OrderPaymentValidationGateway;
import org.springframework.stereotype.Service;

@Service
public class OrderPaymentValidationService implements OrderPaymentValidationUseCase {

    private final OrderPaymentValidationGateway orderPaymentValidationGateway;

    public OrderPaymentValidationService(OrderPaymentValidationGateway orderPaymentValidationGateway) {
        this.orderPaymentValidationGateway = orderPaymentValidationGateway;
    }

    @Override
    public boolean validateOrderPayment(OrderPaymentValidationCommand command) {
        validateCommand(command);
        return orderPaymentValidationGateway.validate(command.orderId(), command.amount());
    }

    private void validateCommand(OrderPaymentValidationCommand command) {
        if (command == null) {
            throw new InvalidCardPaymentRequestException("order payment validation command is required.");
        }
        if (command.orderId() == null) {
            throw new InvalidCardPaymentRequestException("orderId is required for order validation.");
        }
        if (command.amount() == null || command.amount() <= 0) {
            throw new InvalidCardPaymentRequestException("amount must be positive for order validation.");
        }
    }
}
