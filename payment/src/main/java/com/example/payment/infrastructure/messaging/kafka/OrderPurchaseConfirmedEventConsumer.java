package com.example.payment.infrastructure.messaging.kafka;

import com.example.payment.application.dto.EscrowReleaseCommand;
import com.example.payment.application.usecase.EscrowReleaseUseCase;
import com.example.payment.domain.enumtype.ConfirmationType;
import com.example.payment.domain.exception.EscrowAlreadyReleasedException;
import com.example.payment.domain.exception.InvalidOrderPaymentRequestException;
import com.example.payment.infrastructure.messaging.kafka.contract.OrderPurchaseConfirmedMessage;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderPurchaseConfirmedEventConsumer {

    private final EscrowReleaseUseCase escrowReleaseUseCase;

    public OrderPurchaseConfirmedEventConsumer(EscrowReleaseUseCase escrowReleaseUseCase) {
        this.escrowReleaseUseCase = escrowReleaseUseCase;
    }

    @KafkaListener(
            topics = "${payment.kafka.topics.order-purchase-confirmed:order.purchase-confirmed}",
            groupId = "${payment.kafka.consumer-groups.order-purchase-confirmed:payment-service}",
            containerFactory = "orderPurchaseConfirmedKafkaListenerContainerFactory"
    )
    public void listen(OrderPurchaseConfirmedMessage event) {
        validateEvent(event);

        try {
            escrowReleaseUseCase.releaseEscrow(new EscrowReleaseCommand(
                    event.orderId(),
                    event.sellerMemberId(),
                    event.confirmationType()
            ));
        } catch (EscrowAlreadyReleasedException ignored) {
            // Duplicate confirmation event should not release funds twice.
        }
    }

    private void validateEvent(OrderPurchaseConfirmedMessage event) {
        if (event == null) {
            throw new InvalidOrderPaymentRequestException("orderPurchaseConfirmed event is required.");
        }
        if (event.orderId() == null) {
            throw new InvalidOrderPaymentRequestException("orderId is required.");
        }
        if (event.sellerMemberId() == null) {
            throw new InvalidOrderPaymentRequestException("sellerMemberId is required.");
        }
        if (event.confirmationType() != ConfirmationType.MANUAL) {
            throw new InvalidOrderPaymentRequestException("Only MANUAL confirmation event is allowed.");
        }
    }
}
