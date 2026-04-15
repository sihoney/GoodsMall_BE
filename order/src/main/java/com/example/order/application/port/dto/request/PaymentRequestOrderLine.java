package com.example.order.application.port.dto.request;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentRequestOrderLine(
        UUID orderItemId,
        UUID sellerId,
        BigDecimal unitPriceSnapshot,
        int quantity,
        BigDecimal lineTotalPrice
) {
}
