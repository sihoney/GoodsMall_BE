package com.example.order.infrastructure.repository;

import com.example.order.domain.entity.Order;
import com.example.order.domain.enumtype.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderJpaRepository extends JpaRepository<Order, UUID> {

    Page<Order> findByBuyerId(UUID buyerId, Pageable pageable);

    @Query("""
    select o from Order o
    left join fetch o.items
    where o.orderId = :orderId
      and o.buyerId = :buyerId
""")
    Optional<Order> findByOrderIdAndBuyerId(UUID orderId, UUID buyerId);

    Optional<Order> findByOrderId(UUID orderId);

    @Query("""
    select o from Order o
    left join fetch o.items
    where o.status = :status
      and o.deliveredAt < :threshold
""")
    List<Order> findByStatusAndDeliveredAtBefore(OrderStatus status, LocalDateTime threshold);
}
