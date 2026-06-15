package com.example.payment.payment.infrastructure.repository;

import com.example.payment.payment.domain.entity.PaymentRefundItem;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRefundItemJpaRepository extends JpaRepository<PaymentRefundItem, UUID> {

    List<PaymentRefundItem> findAllByRefundId(UUID refundId);
}
