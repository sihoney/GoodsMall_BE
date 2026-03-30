package com.example.payment.infrastructure.messaging.kafka;

import com.example.payment.common.exception.InvalidOrderPaymentRequestException;
import com.example.payment.common.exception.WalletNotFoundException;
import com.example.payment.application.dto.OrderPaymentResult;
import com.example.payment.application.dto.OrderPaymentCommand;
import com.example.payment.application.usecase.OrderPaymentUseCase;
import com.example.payment.domain.service.OrderPaymentResultEventPublisher;
import com.example.payment.infrastructure.messaging.kafka.contract.OrderPaymentFailureReason;
import com.example.payment.infrastructure.messaging.kafka.contract.OrderPaymentRequestedMessage;
import com.example.payment.infrastructure.messaging.kafka.contract.OrderPaymentResultMessage;
import com.example.payment.infrastructure.messaging.kafka.contract.OrderPaymentResultStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
/**
 * 주문 결제 요청 이벤트를 payment 유스케이스로 연결하는 Kafka consumer다.
 * transport 계층에서는 이벤트 유효성 검증과 실패 사유 매핑만 담당하고,
 * 실제 중복 처리와 비즈니스 정책은 order payment usecase에 위임한다.
 */
public class OrderPaymentRequestedEventConsumer {

    private final OrderPaymentUseCase orderPaymentUseCase;
    private final OrderPaymentResultEventPublisher orderPaymentResultEventPublisher;
    private final ObjectMapper objectMapper;

    public OrderPaymentRequestedEventConsumer(
            OrderPaymentUseCase orderPaymentUseCase,
            OrderPaymentResultEventPublisher orderPaymentResultEventPublisher,
            ObjectMapper objectMapper
    ) {
        this.orderPaymentUseCase = orderPaymentUseCase;
        this.orderPaymentResultEventPublisher = orderPaymentResultEventPublisher;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
            topics = "${payment.kafka.topics.order-payment-requested:order.payment-requested}",
            groupId = "${payment.kafka.consumer-groups.order-payment-requested:payment-service}",
            containerFactory = "orderPaymentRequestedKafkaListenerContainerFactory"
    )
    /**
     * 주문 결제 요청 이벤트를 command로 변환해 usecase에 전달한다.
     * usecase 결과나 예외를 다시 Kafka 결과 이벤트로 바꿔 upstream이 상태를 추적할 수 있게 한다.
     */
    public void listen(String eventJson) {
        try {
            OrderPaymentRequestedMessage event = objectMapper.readValue(eventJson, OrderPaymentRequestedMessage.class);
            validateEvent(event);

            try {
                OrderPaymentResult result = orderPaymentUseCase.payOrder(new OrderPaymentCommand(
                        event.orderId(),
                        event.buyerMemberId(),
                        event.sellerMemberId(),
                        event.orderAmount(),
                        event.sellerReceivableAmount(),
                        null
                ));
                orderPaymentResultEventPublisher.publish(successMessage(event, result));
            } catch (WalletNotFoundException e) {
                orderPaymentResultEventPublisher.publish(failureMessage(
                        event,
                        OrderPaymentFailureReason.WALLET_NOT_FOUND,
                        e.getMessage()
                ));
            } catch (IllegalArgumentException e) {
                orderPaymentResultEventPublisher.publish(failureMessage(
                        event,
                        OrderPaymentFailureReason.INSUFFICIENT_BALANCE,
                        e.getMessage()
                ));
            } catch (InvalidOrderPaymentRequestException e) {
                orderPaymentResultEventPublisher.publish(failureMessage(
                        event,
                        OrderPaymentFailureReason.INVALID_REQUEST,
                        e.getMessage()
                ));
            } catch (RuntimeException e) {
                orderPaymentResultEventPublisher.publish(failureMessage(
                        event,
                        OrderPaymentFailureReason.INTERNAL_ERROR,
                        e.getMessage()
                ));
            }
        } catch (Exception e) {
            log.error("Failed to process OrderPaymentRequestedMessage", e);
            throw new RuntimeException("Failed to deserialize OrderPaymentRequestedMessage", e);
        }
    }

    private OrderPaymentResultMessage successMessage(OrderPaymentRequestedMessage event, OrderPaymentResult result) {
        return new OrderPaymentResultMessage(
                newEventId(),
                event.orderId(),
                event.buyerMemberId(),
                event.sellerMemberId(),
                OrderPaymentResultStatus.SUCCESS,
                event.orderAmount(),
                event.sellerReceivableAmount(),
                result.buyerWalletId(),
                result.escrowId(),
                null,
                null,
                LocalDateTime.now()
        );
    }

    private OrderPaymentResultMessage failureMessage(
            OrderPaymentRequestedMessage event,
            OrderPaymentFailureReason reason,
            String failureMessage
    ) {
        return new OrderPaymentResultMessage(
                newEventId(),
                event.orderId(),
                event.buyerMemberId(),
                event.sellerMemberId(),
                OrderPaymentResultStatus.FAILED,
                event.orderAmount(),
                event.sellerReceivableAmount(),
                null,
                null,
                reason,
                failureMessage,
                LocalDateTime.now()
        );
    }

    private String newEventId() {
        return UUID.randomUUID().toString();
    }

    /**
     * consumer 단계에서 계약 필수값만 검증한다.
     * 금액 차감 가능 여부나 멱등 처리 같은 비즈니스 판단은 usecase에서 수행한다.
     */
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
