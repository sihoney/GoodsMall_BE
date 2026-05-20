package com.example.payment.payment.domain.repository;

import com.example.payment.payment.domain.entity.PaymentRefundItem;
import java.util.List;
import java.util.UUID;

public interface PaymentRefundItemRepository {

    List<PaymentRefundItem> saveAll(List<PaymentRefundItem> paymentRefundItems);

    List<PaymentRefundItem> findAllByRefundId(UUID refundId);
}
