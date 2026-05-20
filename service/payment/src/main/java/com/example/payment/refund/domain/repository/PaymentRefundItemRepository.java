package com.example.payment.refund.domain.repository;

import com.example.payment.refund.domain.entity.PaymentRefundItem;
import java.util.List;
import java.util.UUID;

public interface PaymentRefundItemRepository {

    List<PaymentRefundItem> saveAll(List<PaymentRefundItem> paymentRefundItems);

    List<PaymentRefundItem> findAllByRefundId(UUID refundId);
}
