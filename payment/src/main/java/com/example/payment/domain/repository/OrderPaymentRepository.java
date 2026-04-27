package com.example.payment.domain.repository;

import com.example.payment.domain.entity.OrderPayment;
import java.util.Optional;
import java.util.UUID;

public interface OrderPaymentRepository {

    OrderPayment save(OrderPayment orderPayment);

    Optional<OrderPayment> findByOrderId(UUID orderId);

    Optional<OrderPayment> findByOrderIdAndBuyerMemberId(UUID orderId, UUID buyerMemberId);
}
