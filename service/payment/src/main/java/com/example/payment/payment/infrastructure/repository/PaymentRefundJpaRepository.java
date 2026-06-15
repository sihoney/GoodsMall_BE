package com.example.payment.payment.infrastructure.repository;

import com.example.payment.payment.domain.entity.PaymentRefund;
import com.example.payment.payment.domain.enumtype.PaymentRefundStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRefundJpaRepository extends JpaRepository<PaymentRefund, UUID> {

    Optional<PaymentRefund> findByOrderCancelRequestId(UUID orderCancelRequestId);

    Optional<PaymentRefund> findFirstByOrderIdOrderByCreatedAtDesc(UUID orderId);

    List<PaymentRefund> findAllByOrderIdAndRefundStatus(UUID orderId, PaymentRefundStatus refundStatus);
}
