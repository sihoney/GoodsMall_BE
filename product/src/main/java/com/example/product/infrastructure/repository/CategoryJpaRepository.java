package com.example.product.infrastructure.repository;

import com.example.product.domain.entity.Category;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CategoryJpaRepository extends JpaRepository<Category, UUID> {

    @Query("""
                SELECT DISTINCT c FROM Category c
                LEFT JOIN FETCH c.parent
                WHERE c.deletedAt IS NULL
                ORDER BY c.depth ASC, c.sortOrder ASC
            """)
    List<Category> findAllWithParent();

    Optional<Category> findByCategoryIdAndDeletedAtIsNull(UUID categoryId);

    @Query("""
                SELECT c FROM Category c
                WHERE c.depth = :depth
                AND c.deletedAt IS NULL
                ORDER BY c.sortOrder ASC
            """)
    List<Category> findByDepthOrderBySortOrder(@Param("depth") Integer depth);

    @Query("""
                SELECT c FROM Category c
                WHERE c.parent.categoryId = :parentId
                AND c.deletedAt IS NULL
                ORDER BY c.sortOrder ASC
            """)
    List<Category> findByParentIdOrderBySortOrder(@Param("parentId") UUID parentId);

    boolean existsByParent_CategoryIdAndDeletedAtIsNull(UUID parentId);
}
