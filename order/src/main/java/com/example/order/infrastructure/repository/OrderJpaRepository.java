package com.example.order.infrastructure.repository;

import com.example.order.domain.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

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
}
