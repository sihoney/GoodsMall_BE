package com.example.order.application.service;

import com.example.order.domain.entity.Order;
import com.example.order.domain.enumtype.OrderStatus;
import com.example.order.domain.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderAutoConfirmScheduler {

    private static final int AUTO_CONFIRM_DAYS = 7;

    private final OrderRepository orderRepository;

    @Scheduled(fixedDelay = 3600000) // 1시간마다
    @Transactional
    public void autoConfirm() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(AUTO_CONFIRM_DAYS);
        List<Order> orders = orderRepository.findByStatusAndDeliveredAtBefore(OrderStatus.DELIVERED, threshold);

        for (Order order : orders) {
            order.complete();
        }

        if (!orders.isEmpty()) {
            log.info("자동 구매 확정 처리. count={}", orders.size());
        }
    }
}
