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
        // TODO: 재고 복구, 결제 취소 요청 등 보상 처리 구현
        Order order = orderRepository.findByOrderId(event.orderId())
                .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));

        PaymentStatus status = PaymentStatus.from(event.status());

        if (status == PaymentStatus.FAILED) {
            handlePaymentFailed(event);
            return;
        }

        handlePaymentSuccess(order);
    }

    private void handlePaymentFailed(PaymentResultEvent event) {
        log.warn("결제 실패 이벤트 수신. orderId={}", event.orderId());
    }

    private void handlePaymentSuccess(Order order) {
        boolean changed = order.confirm();
        if (!changed) {
            log.warn("order 상태 변경 불가. orderId={}", order.getOrderId());
            return;
        }
        deliveryCreateService.create(order);
    }
}
