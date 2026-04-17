package com.example.ai.infrastructure.client;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ProductCatalogClient {

    List<ProductSnapshot> fetchAllProducts();

    record ProductSnapshot(
            UUID productId,
            String title,
            String categoryName,
            String description,
            String status,
            LocalDateTime sourceUpdatedAt
    ) {
    }
}

