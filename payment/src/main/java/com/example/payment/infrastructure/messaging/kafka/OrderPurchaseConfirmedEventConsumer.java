package com.example.payment.infrastructure.messaging.kafka;

import com.example.payment.common.exception.InvalidOrderPaymentRequestException;
import com.example.payment.application.dto.EscrowReleaseCommand;
import com.example.payment.application.usecase.EscrowReleaseUseCase;
import com.example.payment.domain.enumtype.ConfirmationType;
import com.example.payment.infrastructure.messaging.kafka.contract.OrderPurchaseConfirmedMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
/**
 * 수동 구매확정 이벤트를 escrow release 유스케이스로 연결하는 Kafka consumer다.
 * consumer는 MANUAL 계약 검증과 command 변환만 담당하고, 정산 정책은 usecase에 위임한다.
 */
public class OrderPurchaseConfirmedEventConsumer {

    private final EscrowReleaseUseCase escrowReleaseUseCase;
    private final ObjectMapper objectMapper;

    public OrderPurchaseConfirmedEventConsumer(EscrowReleaseUseCase escrowReleaseUseCase, ObjectMapper objectMapper) {
        this.escrowReleaseUseCase = escrowReleaseUseCase;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
            topics = "${payment.kafka.topics.order-purchase-confirmed:order.purchase-confirmed}",
            groupId = "${payment.kafka.consumer-groups.order-purchase-confirmed:payment-service}",
            containerFactory = "orderPurchaseConfirmedKafkaListenerContainerFactory"
    )
    /**
     * 수동 구매확정 이벤트만 escrow release 요청으로 전달한다.
     * AUTO 구매확정은 scheduler 경로에서 처리되므로 consumer 단계에서 차단한다.
     */
    public void listen(String eventJson) {
        try {
            OrderPurchaseConfirmedMessage event = objectMapper.readValue(eventJson, OrderPurchaseConfirmedMessage.class);
            validateEvent(event);
            escrowReleaseUseCase.releaseEscrow(new EscrowReleaseCommand(
                    event.orderId(),
                    event.sellerMemberId(),
                    event.confirmationType()
            ));
        } catch (Exception e) {
            log.error("Failed to process OrderPurchaseConfirmedMessage", e);
            throw new RuntimeException("Failed to deserialize OrderPurchaseConfirmedMessage", e);
        }
    }

    /**
     * 구매확정 이벤트 계약과 허용 confirmation type을 검증한다.
     */
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
        if (event.confirmedAt() == null) {
            throw new InvalidOrderPaymentRequestException("confirmedAt is required.");
        }
        if (event.confirmationType() != ConfirmationType.MANUAL) {
            throw new InvalidOrderPaymentRequestException("Only MANUAL confirmation event is allowed.");
        }
    }
}
