package com.example.product.infrastructure.repository;

import com.example.product.domain.entity.Product;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProductJpaRepository extends JpaRepository<Product, UUID> {

    @Query("""
        SELECT p
        FROM Product p
        WHERE p.status = 'ACTIVE'
          AND p.deletedAt IS NULL
        """)
    Page<Product> findDisplayProducts(Pageable pageable);

    Page<Product> findBySellerId(UUID sellerId, Pageable pageable);

    Optional<Product> findById(UUID productId);
}
