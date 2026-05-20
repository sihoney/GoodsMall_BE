package com.example.payment.payment.infrastructure.repository;

import com.example.payment.payment.domain.entity.PaymentRefundAllocation;
import com.example.payment.payment.domain.repository.PaymentRefundAllocationRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class PaymentRefundAllocationRepositoryImpl implements PaymentRefundAllocationRepository {

    private final PaymentRefundAllocationJpaRepository paymentRefundAllocationJpaRepository;

    public PaymentRefundAllocationRepositoryImpl(
            PaymentRefundAllocationJpaRepository paymentRefundAllocationJpaRepository
    ) {
        this.paymentRefundAllocationJpaRepository = paymentRefundAllocationJpaRepository;
    }

    @Override
    public List<PaymentRefundAllocation> saveAll(List<PaymentRefundAllocation> allocations) {
        return paymentRefundAllocationJpaRepository.saveAll(allocations);
    }

    @Override
    public List<PaymentRefundAllocation> findAllByRefundId(UUID refundId) {
        return paymentRefundAllocationJpaRepository.findAllByRefundId(refundId);
    }
}
