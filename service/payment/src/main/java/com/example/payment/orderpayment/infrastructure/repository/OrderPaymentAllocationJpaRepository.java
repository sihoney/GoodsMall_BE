package com.example.payment.orderpayment.infrastructure.repository;

import com.example.payment.orderpayment.domain.entity.OrderPaymentAllocation;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderPaymentAllocationJpaRepository extends JpaRepository<OrderPaymentAllocation, UUID> {

    List<OrderPaymentAllocation> findAllByOrderPaymentId(UUID orderPaymentId);
}
