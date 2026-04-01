package com.example.order.domain.repository;

import com.example.order.domain.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface OrderRepository {

    Order save(Order order);

    Page<Order> findByBuyerId(UUID buyerId, Pageable pageable);

    Optional<Order> findByOrderIdAndBuyerId(UUID orderId, UUID buyerId);

    Optional<Order> findByOrderId(UUID orderId);
}
