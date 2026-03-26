package com.example.payment.infrastructure.messaging.kafka;

import com.example.payment.application.dto.OrderPaymentCommand;
import com.example.payment.application.usecase.OrderPaymentUseCase;
import com.example.payment.domain.exception.InvalidOrderPaymentRequestException;
import com.example.payment.domain.exception.OrderPaymentAlreadyCompletedException;
import com.example.payment.infrastructure.messaging.kafka.contract.OrderPaymentRequestedMessage;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderPaymentRequestedEventConsumer {

    private final OrderPaymentUseCase orderPaymentUseCase;

    public OrderPaymentRequestedEventConsumer(OrderPaymentUseCase orderPaymentUseCase) {
        this.orderPaymentUseCase = orderPaymentUseCase;
    }

    @KafkaListener(
            topics = "${payment.kafka.topics.order-payment-requested:order.payment-requested}",
            groupId = "${payment.kafka.consumer-groups.order-payment-requested:payment-service}",
            containerFactory = "orderPaymentRequestedKafkaListenerContainerFactory"
    )
    public void listen(OrderPaymentRequestedMessage event) {
        validateEvent(event);

        try {
            orderPaymentUseCase.payOrder(new OrderPaymentCommand(
                    event.orderId(),
                    event.buyerMemberId(),
                    event.sellerMemberId(),
                    event.orderAmount(),
                    event.sellerReceivableAmount(),
                    null
            ));
        } catch (OrderPaymentAlreadyCompletedException ignored) {
            // Duplicate payment request event should not create a second escrow.
        }
    }

    private void validateEvent(OrderPaymentRequestedMessage event) {
        if (event == null) {
            throw new InvalidOrderPaymentRequestException("orderPaymentRequested event is required.");
        }
        if (event.orderId() == null) {
            throw new InvalidOrderPaymentRequestException("orderId is required.");
        }
        if (event.buyerMemberId() == null) {
            throw new InvalidOrderPaymentRequestException("buyerMemberId is required.");
        }
        if (event.sellerMemberId() == null) {
            throw new InvalidOrderPaymentRequestException("sellerMemberId is required.");
        }
        if (event.orderAmount() == null || event.orderAmount() <= 0) {
            throw new InvalidOrderPaymentRequestException("orderAmount must be positive.");
        }
        if (event.sellerReceivableAmount() == null || event.sellerReceivableAmount() <= 0) {
            throw new InvalidOrderPaymentRequestException("sellerReceivableAmount must be positive.");
        }
    }
}
