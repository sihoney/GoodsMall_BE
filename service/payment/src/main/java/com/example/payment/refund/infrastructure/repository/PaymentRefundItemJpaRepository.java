package com.example.payment.refund.infrastructure.repository;

import com.example.payment.refund.domain.entity.PaymentRefundItem;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRefundItemJpaRepository extends JpaRepository<PaymentRefundItem, UUID> {

    List<PaymentRefundItem> findAllByRefundId(UUID refundId);
}
