package com.example.payment.infrastructure.messaging.kafka;

import com.example.payment.application.dto.EscrowReleaseScheduleCommand;
import com.example.payment.application.usecase.EscrowReleaseScheduleUseCase;
import com.example.payment.domain.exception.EscrowReleaseAlreadyScheduledException;
import com.example.payment.domain.exception.InvalidOrderPaymentRequestException;
import com.example.payment.infrastructure.messaging.kafka.contract.OrderDeliveryCompletedMessage;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderDeliveryCompletedEventConsumer {

    private final EscrowReleaseScheduleUseCase escrowReleaseScheduleUseCase;

    public OrderDeliveryCompletedEventConsumer(EscrowReleaseScheduleUseCase escrowReleaseScheduleUseCase) {
        this.escrowReleaseScheduleUseCase = escrowReleaseScheduleUseCase;
    }

    @KafkaListener(
            topics = "${payment.kafka.topics.order-delivery-completed:order.delivery-completed}",
            groupId = "${payment.kafka.consumer-groups.order-delivery-completed:payment-service}",
            containerFactory = "orderDeliveryCompletedKafkaListenerContainerFactory"
    )
    public void listen(OrderDeliveryCompletedMessage event) {
        validateEvent(event);

        try {
            escrowReleaseScheduleUseCase.scheduleRelease(new EscrowReleaseScheduleCommand(
                    event.orderId(),
                    event.deliveredAt()
            ));
        } catch (EscrowReleaseAlreadyScheduledException ignored) {
            // Duplicate delivery completed event should not reschedule releaseAt.
        }
    }

    private void validateEvent(OrderDeliveryCompletedMessage event) {
        if (event == null) {
            throw new InvalidOrderPaymentRequestException("orderDeliveryCompleted event is required.");
        }
        if (event.orderId() == null) {
            throw new InvalidOrderPaymentRequestException("orderId is required.");
        }
        if (event.deliveredAt() == null) {
            throw new InvalidOrderPaymentRequestException("deliveredAt is required.");
        }
    }
}
