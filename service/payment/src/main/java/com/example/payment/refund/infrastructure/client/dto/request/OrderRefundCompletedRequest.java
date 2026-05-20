package com.example.payment.refund.infrastructure.client.dto.request;

import java.util.List;
import java.util.UUID;

public record OrderRefundCompletedRequest(
        UUID orderId,
        List<UUID> orderItemIds
) {
}
