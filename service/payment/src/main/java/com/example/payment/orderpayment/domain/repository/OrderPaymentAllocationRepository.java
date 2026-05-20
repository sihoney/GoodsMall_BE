package com.example.payment.orderpayment.domain.repository;

import com.example.payment.orderpayment.domain.entity.OrderPaymentAllocation;
import java.util.List;
import java.util.UUID;

public interface OrderPaymentAllocationRepository {

    List<OrderPaymentAllocation> saveAll(List<OrderPaymentAllocation> allocations);

    List<OrderPaymentAllocation> findAllByOrderPaymentId(UUID orderPaymentId);
}
