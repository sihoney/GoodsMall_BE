package com.example.ai.domain.repository;

import com.example.ai.domain.entity.ProductEmbedding;
import com.example.ai.domain.model.SimilarProductMatch;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductEmbeddingRepository {

    Optional<ProductEmbedding> findByProductId(UUID productId);

    List<SimilarProductMatch> findSimilarActive(UUID productId, String embeddingVector, int limit);

    ProductEmbedding save(ProductEmbedding productEmbedding);
}
