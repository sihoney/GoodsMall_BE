package com.example.product.infrastructure.repository;

import com.example.product.domain.entity.ProductImage;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProductImageJpaRepository extends JpaRepository<ProductImage, UUID> {

    @Query("""
        SELECT pi
        FROM ProductImage pi
        WHERE pi.productId = :productId
          AND pi.thumbnail = true
        """)
    Optional<ProductImage> findThumbnailByProductId(UUID productId);
}
