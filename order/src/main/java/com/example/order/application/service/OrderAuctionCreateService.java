package com.example.order.application.service;

import com.example.order.domain.entity.Order;
import com.example.order.domain.repository.OrderRepository;
import com.example.order.infrastructure.kafka.event.AuctionWonEvent;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderAuctionCreateService {

    private final OrderRepository orderRepository;
    private final OrderNumberGenerator orderNumberGenerator;

    @Transactional
    public void createFromAuctionWon(UUID auctionId, UUID winnerId, AuctionWonEvent event) {

        if (orderRepository.findByAuctionId(auctionId).isPresent()) {
            log.warn("경매 주문이 이미 존재합니다. auctionId={}", auctionId);
            return;
        }

        String orderNumber = orderNumberGenerator.generateUnique();

        Order order = Order.createForAuction(
                orderNumber,
                auctionId,
                winnerId,
                event.productTitle(),
                event.thumbnailKey()
        );

        order.addItem(
                event.productId(),
                event.sellerId(),
                event.productTitle(),
                event.orderPrice(),
                1,
                event.thumbnailKey()
        );

        orderRepository.save(order);
        log.info("경매 주문 사전 생성 완료. auctionId={}, orderId={}", auctionId, order.getOrderId());
    }
}
