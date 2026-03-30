package com.example.product.infrastructure.repository;

import com.example.product.domain.entity.Product;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductJpaRepository extends JpaRepository<Product, UUID> {

    @Query("""
        SELECT p
        FROM Product p
        WHERE p.status = 'ACTIVE'
          AND p.deletedAt IS NULL
        """)
    Page<Product> findDisplayProducts(Pageable pageable);

    @Query("""
        SELECT p
        FROM Product p
        WHERE p.category.categoryId = :categoryId
          AND p.status = 'ACTIVE'
          AND p.deletedAt IS NULL
        """)
    Page<Product> findDisplayProductsByCategoryId(UUID categoryId, Pageable pageable);

    @Query("""
        SELECT p
        FROM Product p
        WHERE p.category.categoryId IN :categoryIds
          AND p.status = 'ACTIVE'
          AND p.deletedAt IS NULL
        """)
    Page<Product> findDisplayProductsByCategoryIds(@Param("categoryIds") List<UUID> categoryIds, Pageable pageable);

    @Query("""
        SELECT p
        FROM Product p
        WHERE p.status = 'ACTIVE'
          AND p.deletedAt IS NULL
          AND (:categoryIds IS NULL OR SIZE(:categoryIds) = 0 OR p.category.categoryId IN :categoryIds)
          AND (:keyword IS NULL OR :keyword = '' OR p.title LIKE %:keyword% OR p.description LIKE %:keyword%)
          AND (:minPrice IS NULL OR p.price >= :minPrice)
          AND (:maxPrice IS NULL OR p.price <= :maxPrice)
        """)
    Page<Product> findDisplayProductsWithFilters(
            @Param("categoryIds") List<UUID> categoryIds,
            @Param("keyword") String keyword,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable
    );

    Page<Product> findBySellerId(UUID sellerId, Pageable pageable);

    Optional<Product> findById(UUID productId);

}
