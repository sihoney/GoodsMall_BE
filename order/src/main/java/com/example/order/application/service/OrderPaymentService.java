package com.example.order.application.service;

import com.example.order.common.exception.CustomException;
import com.example.order.common.exception.ErrorCode;
import com.example.order.domain.entity.Order;
import com.example.order.domain.enumtype.PaymentStatus;
import com.example.order.domain.repository.OrderRepository;
import com.example.order.infrastructure.kafka.event.PaymentResultEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderPaymentService {

    private final OrderRepository orderRepository;
    private final DeliveryCreateService deliveryCreateService;

    @Transactional
    public void handlePaymentResult(PaymentResultEvent event) {
        Order order = orderRepository.findByOrderId(event.orderId())
                .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));

        PaymentStatus status = PaymentStatus.from(event.status());

        if (status == PaymentStatus.FAILED) {
            log.warn("결제 실패 이벤트 수신. orderId={}", event.orderId());
            return;
        }

        boolean changed;
        try {
            changed = order.confirm(event.amount());
        } catch (CustomException e) {
            if (e.getErrorCode() == ErrorCode.INVALID_PAYMENT_AMOUNT) {
                log.error("금액 불일치. orderId={}, orderAmount={}, paymentAmount={}",
                        event.orderId(), order.getTotalPrice(), event.amount());
                return;
            }
            throw e;
        }
        if (!changed) {
            return;
        }

        deliveryCreateService.create(order);
    }
}
