package com.example.payment.infrastructure.messaging.kafka;

import com.example.payment.application.dto.OrderPaymentCommand;
import com.example.payment.application.dto.OrderPaymentResult;
import com.example.payment.application.dto.OrderPaymentSellerCommand;
import com.example.payment.application.usecase.OrderPaymentUseCase;
import com.example.payment.common.exception.InvalidOrderPaymentRequestException;
import com.example.payment.common.exception.WalletNotFoundException;
import com.example.payment.domain.service.OrderPaymentResultEventPublisher;
import com.example.payment.infrastructure.messaging.kafka.contract.OrderPaymentFailureReason;
import com.example.payment.infrastructure.messaging.kafka.contract.OrderPaymentRequestedLineMessage;
import com.example.payment.infrastructure.messaging.kafka.contract.OrderPaymentRequestedMessage;
import com.example.payment.infrastructure.messaging.kafka.contract.OrderPaymentResultMessage;
import com.example.payment.infrastructure.messaging.kafka.contract.OrderPaymentResultStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
/**
 * order의 주문 결제 요청 이벤트를 payment 유스케이스로 연결하는 Kafka consumer다.
 * orderLines를 seller별 내부 정산 단위로 집계한 뒤 결제 유스케이스에 전달한다.
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
     * 주문 결제 요청 이벤트를 읽어 seller별 결제 command로 변환하고, 성공/실패 결과 이벤트를 발행한다.
     */
    public void listen(String eventJson) {
        try {
            OrderPaymentRequestedMessage event = objectMapper.readValue(eventJson, OrderPaymentRequestedMessage.class);
            validateEvent(event);

            try {
                // 외부 order 이벤트의 line 목록을 seller별 내부 정산 단위로 변환한다.
                OrderPaymentResult result = orderPaymentUseCase.payOrder(new OrderPaymentCommand(
                        event.orderId(),
                        event.buyerId(),
                        toAmount(event.totalPrice()),
                        aggregateSellerPayments(event.orderLines()),
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

    /**
     * 결제 성공 결과를 order-payment-result 계약 메시지로 변환한다.
     */
    private OrderPaymentResultMessage successMessage(OrderPaymentRequestedMessage event, OrderPaymentResult result) {
        return new OrderPaymentResultMessage(
                newEventId(),
                event.orderId(),
                event.buyerId(),
                singleSellerId(event),
                OrderPaymentResultStatus.SUCCESS,
                toAmount(event.totalPrice()),
                singleSellerAmount(event),
                result.buyerWalletId(),
                singleEscrowId(result),
                null,
                null,
                LocalDateTime.now()
        );
    }

    /**
     * 결제 실패 결과를 order-payment-result 계약 메시지로 변환한다.
     */
    private OrderPaymentResultMessage failureMessage(
            OrderPaymentRequestedMessage event,
            OrderPaymentFailureReason reason,
            String failureMessage
    ) {
        return new OrderPaymentResultMessage(
                newEventId(),
                event.orderId(),
                event.buyerId(),
                singleSellerId(event),
                OrderPaymentResultStatus.FAILED,
                toAmount(event.totalPrice()),
                singleSellerAmount(event),
                null,
                null,
                reason,
                failureMessage,
                LocalDateTime.now()
        );
    }

    /**
     * 결과 이벤트용 eventId를 새로 생성한다.
     */
    private String newEventId() {
        return UUID.randomUUID().toString();
    }

    /**
     * order가 보내는 주문 결제 요청 계약의 필수 필드와 금액 합계를 검증한다.
     */
    private void validateEvent(OrderPaymentRequestedMessage event) {
        if (event == null) {
            throw new InvalidOrderPaymentRequestException("orderPaymentRequested event is required.");
        }
        if (event.orderId() == null) {
            throw new InvalidOrderPaymentRequestException("orderId is required.");
        }
        if (event.buyerId() == null) {
            throw new InvalidOrderPaymentRequestException("buyerId is required.");
        }
        if (event.totalPrice() == null || event.totalPrice().signum() <= 0) {
            throw new InvalidOrderPaymentRequestException("totalPrice must be positive.");
        }
        if (event.orderLines() == null || event.orderLines().isEmpty()) {
            throw new InvalidOrderPaymentRequestException("orderLines must not be empty.");
        }

        BigDecimal lineTotalAmount = BigDecimal.ZERO;
        for (OrderPaymentRequestedLineMessage orderLine : event.orderLines()) {
            if (orderLine == null) {
                throw new InvalidOrderPaymentRequestException("orderLines must not contain null.");
            }
            if (orderLine.orderItemId() == null) {
                throw new InvalidOrderPaymentRequestException("orderItemId is required.");
            }
            if (orderLine.sellerId() == null) {
                throw new InvalidOrderPaymentRequestException("sellerId is required.");
            }
            if (orderLine.quantity() == null || orderLine.quantity() <= 0) {
                throw new InvalidOrderPaymentRequestException("quantity must be positive.");
            }
            if (orderLine.lineTotalPrice() == null || orderLine.lineTotalPrice().signum() <= 0) {
                throw new InvalidOrderPaymentRequestException("lineTotalPrice must be positive.");
            }
            lineTotalAmount = lineTotalAmount.add(orderLine.lineTotalPrice());
        }

        if (lineTotalAmount.compareTo(event.totalPrice()) != 0) {
            throw new InvalidOrderPaymentRequestException("lineTotalPrice total must equal totalPrice.");
        }
    }

    /**
     * 같은 seller의 여러 line 금액을 합산해 payment 내부 seller별 escrow 단위로 변환한다.
     */
    private List<OrderPaymentSellerCommand> aggregateSellerPayments(List<OrderPaymentRequestedLineMessage> orderLines) {
        // 같은 seller의 여러 주문 라인은 payment 내부에서 하나의 escrow 금액으로 합산한다.
        Map<UUID, Long> sellerPaymentMap = orderLines.stream()
                .collect(Collectors.groupingBy(
                        OrderPaymentRequestedLineMessage::sellerId,
                        Collectors.summingLong(line -> toAmount(line.lineTotalPrice()))
                ));

        return sellerPaymentMap.entrySet().stream()
                .map(entry -> new OrderPaymentSellerCommand(entry.getKey(), entry.getValue()))
                .toList();
    }

    /**
     * 외부 이벤트 금액을 payment 내부 Long 금액 모델로 변환한다.
     */
    private Long toAmount(BigDecimal amount) {
        try {
            return amount.longValueExact();
        } catch (ArithmeticException e) {
            // 현재 payment 금액 모델은 Long 정수 단위이므로, 소수 금액은 계약 위반으로 본다.
            throw new InvalidOrderPaymentRequestException("price must be an exact whole amount.");
        }
    }

    /**
     * 단일 seller 주문인 경우에만 결과 이벤트의 sellerMemberId를 채운다.
     */
    private UUID singleSellerId(OrderPaymentRequestedMessage event) {
        List<OrderPaymentSellerCommand> sellerPayments = aggregateSellerPayments(event.orderLines());
        return sellerPayments.size() == 1 ? sellerPayments.get(0).sellerMemberId() : null;
    }

    /**
     * 단일 seller 주문인 경우에만 결과 이벤트의 seller 금액을 채운다.
     */
    private Long singleSellerAmount(OrderPaymentRequestedMessage event) {
        List<OrderPaymentSellerCommand> sellerPayments = aggregateSellerPayments(event.orderLines());
        return sellerPayments.size() == 1 ? sellerPayments.get(0).sellerReceivableAmount() : null;
    }

    /**
     * escrow가 하나만 생성된 경우에만 결과 이벤트의 escrowId를 채운다.
     */
    private UUID singleEscrowId(OrderPaymentResult result) {
        return result.escrowIds().size() == 1 ? result.escrowIds().get(0) : null;
    }
}
