package com.example.order.infrastructure.client.dto.request;

import java.math.BigDecimal;
import java.util.UUID;

public record ExternalOrderLineRequest(
        UUID orderItemId,
        UUID sellerId,
        BigDecimal unitPriceSnapshot,
        int quantity,
        BigDecimal lineTotalPrice
) {
}
