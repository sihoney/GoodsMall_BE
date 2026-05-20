package com.example.payment.refund.infrastructure.repository;

import com.example.payment.refund.domain.entity.PaymentRefundAllocation;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRefundAllocationJpaRepository extends JpaRepository<PaymentRefundAllocation, UUID> {

    List<PaymentRefundAllocation> findAllByRefundId(UUID refundId);
}
