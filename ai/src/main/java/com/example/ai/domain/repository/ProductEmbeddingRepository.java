package com.example.ai.domain.repository;

import com.example.ai.domain.entity.ProductEmbedding;
import java.util.Optional;
import java.util.UUID;

public interface ProductEmbeddingRepository {

    Optional<ProductEmbedding> findByProductId(UUID productId);

    ProductEmbedding save(ProductEmbedding productEmbedding);
}
