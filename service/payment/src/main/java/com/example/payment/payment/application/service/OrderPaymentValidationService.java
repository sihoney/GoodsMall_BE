package com.example.payment.payment.application.service;

import com.example.payment.payment.application.dto.OrderPaymentValidationCommand;
import com.example.payment.payment.application.usecase.OrderPaymentValidationUseCase;
import com.example.payment.common.exception.InvalidCardPaymentRequestException;
import com.example.payment.payment.domain.service.OrderPaymentValidationData;
import com.example.payment.payment.domain.service.OrderPaymentValidationGateway;
import org.springframework.stereotype.Service;

@Service
public class OrderPaymentValidationService implements OrderPaymentValidationUseCase {

    private final OrderPaymentValidationGateway orderPaymentValidationGateway;

    public OrderPaymentValidationService(OrderPaymentValidationGateway orderPaymentValidationGateway) {
        this.orderPaymentValidationGateway = orderPaymentValidationGateway;
    }

    @Override
    public OrderPaymentValidationData validateOrderPayment(OrderPaymentValidationCommand command) {
        validateCommand(command);
        return orderPaymentValidationGateway.validate(command.orderId(), command.buyerId(), command.amount());
    }

    private void validateCommand(OrderPaymentValidationCommand command) {
        if (command == null) {
            throw new InvalidCardPaymentRequestException("二쇰Ц 寃곗젣 寃利??붿껌???꾩슂?⑸땲??");
        }
        if (command.orderId() == null) {
            throw new InvalidCardPaymentRequestException("二쇰Ц 寃곗젣 寃利앹쓣 ?꾪븳 二쇰Ц ID???꾩닔?낅땲??");
        }
        if (command.buyerId() == null) {
            throw new InvalidCardPaymentRequestException("二쇰Ц 寃곗젣 寃利앹쓣 ?꾪븳 援щℓ??ID???꾩닔?낅땲??");
        }
        if (command.amount() == null || command.amount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new InvalidCardPaymentRequestException("二쇰Ц 寃곗젣 寃利?湲덉븸? 0蹂대떎 而ㅼ빞 ?⑸땲??");
        }
    }
}
