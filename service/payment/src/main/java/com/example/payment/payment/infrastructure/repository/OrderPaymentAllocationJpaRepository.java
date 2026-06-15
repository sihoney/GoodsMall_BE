package com.example.payment.payment.infrastructure.repository;

import com.example.payment.payment.domain.entity.OrderPaymentAllocation;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderPaymentAllocationJpaRepository extends JpaRepository<OrderPaymentAllocation, UUID> {

    List<OrderPaymentAllocation> findAllByOrderPaymentId(UUID orderPaymentId);
}
