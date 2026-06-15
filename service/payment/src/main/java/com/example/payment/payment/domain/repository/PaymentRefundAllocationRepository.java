package com.example.payment.payment.domain.repository;

import com.example.payment.payment.domain.entity.PaymentRefundAllocation;
import java.util.List;
import java.util.UUID;

public interface PaymentRefundAllocationRepository {

    List<PaymentRefundAllocation> saveAll(List<PaymentRefundAllocation> allocations);

    List<PaymentRefundAllocation> findAllByRefundId(UUID refundId);
}
