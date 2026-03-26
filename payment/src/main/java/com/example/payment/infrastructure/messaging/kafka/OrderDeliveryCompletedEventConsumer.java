package com.example.payment.infrastructure.messaging.kafka;

import com.example.payment.application.dto.EscrowReleaseScheduleCommand;
import com.example.payment.application.usecase.EscrowReleaseScheduleUseCase;
import com.example.payment.common.exception.InvalidOrderPaymentRequestException;
import com.example.payment.infrastructure.messaging.kafka.contract.OrderDeliveryCompletedMessage;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
/**
 * 배송완료 이벤트를 escrow 자동 구매확정 예약 유스케이스로 연결하는 Kafka consumer다.
 * consumer는 계약 검증과 command 변환만 담당하고, 예약 가능 여부 판단은 usecase에 맡긴다.
 */
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
    /**
     * 배송완료 이벤트를 수신하면 deliveredAt 기준의 release schedule 요청으로 변환한다.
     * 중복 배송완료 이벤트는 usecase의 no-op 정책에서 흡수한다.
     */
    public void listen(OrderDeliveryCompletedMessage event) {
        validateEvent(event);
        escrowReleaseScheduleUseCase.scheduleRelease(new EscrowReleaseScheduleCommand(
                event.orderId(),
                event.deliveredAt()
        ));
    }

    /**
     * 배송완료 계약에서 필요한 필수 필드만 확인한다.
     */
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
