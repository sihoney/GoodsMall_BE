package com.example.payment.orderpayment.infrastructure.repository;

import com.example.payment.orderpayment.domain.entity.OrderPayment;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderPaymentJpaRepository extends JpaRepository<OrderPayment, UUID> {

    Optional<OrderPayment> findByOrderId(UUID orderId);

    Optional<OrderPayment> findByOrderIdAndBuyerMemberId(UUID orderId, UUID buyerMemberId);
}
