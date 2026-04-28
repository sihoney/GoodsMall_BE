package com.example.payment.infrastructure.repository;

import com.example.payment.domain.entity.OrderPayment;
import com.example.payment.domain.repository.OrderPaymentRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class OrderPaymentRepositoryImpl implements OrderPaymentRepository {

    private final OrderPaymentJpaRepository orderPaymentJpaRepository;

    public OrderPaymentRepositoryImpl(OrderPaymentJpaRepository orderPaymentJpaRepository) {
        this.orderPaymentJpaRepository = orderPaymentJpaRepository;
    }

    @Override
    public OrderPayment save(OrderPayment orderPayment) {
        return orderPaymentJpaRepository.save(orderPayment);
    }

    @Override
    public Optional<OrderPayment> findByOrderId(UUID orderId) {
        return orderPaymentJpaRepository.findByOrderId(orderId);
    }

    @Override
    public Optional<OrderPayment> findByOrderIdAndBuyerMemberId(UUID orderId, UUID buyerMemberId) {
        return orderPaymentJpaRepository.findByOrderIdAndBuyerMemberId(orderId, buyerMemberId);
    }
}
