package com.example.ai.infrastructure.repository;

import com.example.ai.domain.entity.ProductEmbedding;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductEmbeddingJpaRepository extends JpaRepository<ProductEmbedding, UUID> {

    Optional<ProductEmbedding> findByProductId(UUID productId);

    @Query(value = """
            SELECT pe.product_id AS productId,
                   (pe.embedding <=> CAST(:embeddingVector AS vector)) AS distance
            FROM ai.product_embedding pe
            WHERE pe.is_active = true
              AND pe.product_id <> :productId
            ORDER BY pe.embedding <=> CAST(:embeddingVector AS vector)
            LIMIT :limit
            """, nativeQuery = true)
    List<SimilarProductProjection> findSimilarActive(
            @Param("productId") UUID productId,
            @Param("embeddingVector") String embeddingVector,
            @Param("limit") int limit
    );

    interface SimilarProductProjection {
        UUID getProductId();

        Double getDistance();
    }
}
