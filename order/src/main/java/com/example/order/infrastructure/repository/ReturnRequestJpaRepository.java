package com.example.order.infrastructure.repository;

import com.example.order.domain.entity.ReturnRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ReturnRequestJpaRepository extends JpaRepository<ReturnRequest, UUID> {

    boolean existsByOrderItem_OrderItemId(UUID orderItemId);
}
