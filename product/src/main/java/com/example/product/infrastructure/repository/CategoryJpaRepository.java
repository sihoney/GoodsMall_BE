package com.example.product.infrastructure.repository;

import com.example.product.domain.entity.Category;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryJpaRepository extends JpaRepository<Category, UUID> {

    List<Category> findAllByDeletedAtIsNull();

    Optional<Category> findByCategoryIdAndDeletedAtIsNull(UUID categoryId);

    List<Category> findByDepthAndDeletedAtIsNull(Integer depth);

    List<Category> findByParentAndDeletedAtIsNull(Category category);

    boolean existsByParent_CategoryIdAndDeletedAtIsNull(UUID parentId);
}
