package com.example.payment.payment.domain.repository;

import com.example.payment.payment.domain.entity.PaymentRefund;
import com.example.payment.payment.domain.enumtype.PaymentRefundStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRefundRepository {

    PaymentRefund save(PaymentRefund paymentRefund);

    Optional<PaymentRefund> findByRefundId(UUID refundId);

    Optional<PaymentRefund> findByOrderCancelRequestId(UUID orderCancelRequestId);

    Optional<PaymentRefund> findLatestByOrderId(UUID orderId);

    List<PaymentRefund> findAllByOrderIdAndRefundStatus(UUID orderId, PaymentRefundStatus refundStatus);
}
