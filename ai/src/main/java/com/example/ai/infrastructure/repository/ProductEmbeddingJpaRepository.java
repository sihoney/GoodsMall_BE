package com.example.ai.infrastructure.repository;

import com.example.ai.domain.entity.ProductEmbedding;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductEmbeddingJpaRepository extends JpaRepository<ProductEmbedding, UUID> {

    Optional<ProductEmbedding> findByProductId(UUID productId);
}
