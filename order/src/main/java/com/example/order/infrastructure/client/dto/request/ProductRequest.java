package com.example.order.infrastructure.client.dto.request;

import java.util.UUID;

public record ProductRequest(
        UUID productId,
        Integer quantity
) {
}
