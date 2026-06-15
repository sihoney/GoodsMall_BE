package com.example.ai.infrastructure.repository;

import com.example.ai.domain.entity.ProductEmbedding;
import com.example.ai.domain.model.SimilarProductMatch;
import com.example.ai.domain.repository.ProductEmbeddingRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ProductEmbeddingRepositoryImpl implements ProductEmbeddingRepository {

    private final ProductEmbeddingJpaRepository jpaRepository;

    @Override
    public Optional<ProductEmbedding> findByProductId(UUID productId) {
        return jpaRepository.findByProductId(productId);
    }

    @Override
    public List<SimilarProductMatch> findSimilarActive(UUID productId, String embeddingVector, int limit) {
        return jpaRepository.findSimilarActive(productId, embeddingVector, limit)
                .stream()
                .map(row -> new SimilarProductMatch(
                        row.getProductId(),
                        row.getDistance() == null ? 1.0d : row.getDistance()
                ))
                .toList();
    }

    @Override
    public ProductEmbedding save(ProductEmbedding productEmbedding) {
        return jpaRepository.save(productEmbedding);
    }
}
