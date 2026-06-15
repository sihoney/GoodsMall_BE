package com.example.order.application.service;

import com.example.order.common.exception.CustomException;
import com.example.order.common.exception.ErrorCode;
import com.example.order.domain.entity.Order;
import com.example.order.domain.entity.OutboxEvent;
import com.example.order.domain.enumtype.PaymentStatus;
import com.example.order.domain.repository.OrderRepository;
import com.example.order.domain.repository.OutboxRepository;
import com.example.order.infrastructure.kafka.OutboxEventSaver;
import com.example.order.infrastructure.kafka.KafkaTopics;
import com.example.order.infrastructure.kafka.event.OrderConfirmedEvent;
import com.example.order.infrastructure.kafka.event.PaymentFailedEvent;
import com.example.order.infrastructure.kafka.event.PaymentResultEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderPaymentService {

    private final OrderRepository orderRepository;
    private final DeliveryCreateService deliveryCreateService;
    private final OutboxRepository outboxRepository;
    private final OutboxEventSaver outboxEventSaver;
    private final ObjectMapper objectMapper;

    @Transactional
    public void handlePaymentResult(PaymentResultEvent event) {
        Order order = orderRepository.findByOrderId(event.orderId())
                .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));

        PaymentStatus status = PaymentStatus.from(event.status());

        if (status == PaymentStatus.FAILED) {
            handlePaymentFailed(order);
            return;
        }

        handlePaymentSuccess(order);
    }

    private void handlePaymentFailed(Order order) {
        log.warn("결제 실패 이벤트 수신. orderId={}", order.getOrderId());
        order.cancelByPaymentFailure();
        try {
            String payload = objectMapper.writeValueAsString(PaymentFailedEvent.envelopeOf(order));
            outboxEventSaver.save(OutboxEvent.create(KafkaTopics.PAYMENT_FAILED, order.getOrderId().toString(), payload));
        } catch (Exception e) {
            log.error("PaymentFailedEvent Outbox 저장 실패. orderId={}", order.getOrderId(), e);
        }
    }

    private void handlePaymentSuccess(Order order) {
        boolean changed = order.confirm();
        if (!changed) {
            log.warn("order 상태 변경 불가. orderId={}", order.getOrderId());
            return;
        }
        deliveryCreateService.create(order);
        try {
            String payload = objectMapper.writeValueAsString(OrderConfirmedEvent.envelopeOf(order));
            outboxRepository.save(OutboxEvent.create(KafkaTopics.ORDER_CONFIRMED, order.getOrderId().toString(), payload));
        } catch (Exception e) {
            log.error("OrderConfirmedEvent Outbox 저장 실패. orderId={}", order.getOrderId(), e);
        }
    }
}
