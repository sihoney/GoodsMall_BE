package com.example.payment.refund.domain.service;

import java.util.List;
import java.util.UUID;

public interface OrderRefundNotificationGateway {

    boolean notifyRefundCompleted(UUID orderId, List<UUID> orderItemIds);
}
