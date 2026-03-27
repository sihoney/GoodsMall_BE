package com.example.order.infrastructure.repository;

import com.example.order.domain.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OrderJpaRepository extends JpaRepository<Order, UUID> {

    Page<Order> findByBuyerId(UUID buyerId, Pageable pageable);
}
