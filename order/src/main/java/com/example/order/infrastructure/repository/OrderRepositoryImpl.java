package com.example.order.infrastructure.repository;

import com.example.order.domain.entity.Order;
import com.example.order.domain.enumtype.OrderStatus;
import com.example.order.domain.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepository {

    private final OrderJpaRepository orderJpaRepository;

    @Override
    public Order save(Order order) {
        return orderJpaRepository.save(order);
    }

    @Override
    public Page<Order> findByBuyerId(UUID buyerId, Pageable pageable) {
        return orderJpaRepository.findByBuyerId(buyerId, pageable);
    }

    @Override
    public Optional<Order> findByOrderIdAndBuyerId(UUID orderId, UUID buyerId) {
        return orderJpaRepository.findByOrderIdAndBuyerId(orderId, buyerId);
    }

    @Override
    public Optional<Order> findByOrderId(UUID orderId) {
        return orderJpaRepository.findByOrderId(orderId);
    }

    @Override
    public List<Order> findByStatusAndDeliveredAtBefore(OrderStatus status, LocalDateTime threshold) {
        return orderJpaRepository.findByStatusAndDeliveredAtBefore(status, threshold);
    }

    @Override
    public boolean existsByOrderNumber(String orderNumber) {
        return orderJpaRepository.existsByOrderNumber(orderNumber);
    }

    @Override
    public Optional<Order> findByAuctionId(UUID auctionId) {
        return orderJpaRepository.findByAuctionId(auctionId);
    }
}
