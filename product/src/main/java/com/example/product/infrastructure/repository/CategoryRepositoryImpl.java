package com.example.product.infrastructure.repository;

import com.example.product.domain.entity.Category;
import com.example.product.domain.repository.CategoryRepository;
import com.example.product.common.exception.CategoryNotFoundException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CategoryRepositoryImpl implements CategoryRepository {
    private final CategoryJpaRepository jpaRepository;

    @Override
    public Category save(Category category) {
        return jpaRepository.save(category);
    }

    @Override
    public List<Category> findAll() {
        return jpaRepository.findAllWithParent();
    }

    @Override
    public Category findById(UUID categoryId) {
        return jpaRepository.findByCategoryIdAndDeletedAtIsNull(categoryId)
                .orElseThrow(CategoryNotFoundException::new);
    }

    @Override
    public List<Category> findByDepth(Integer depth) {
        return jpaRepository.findByDepthOrderBySortOrder(depth);
    }

    @Override
    public List<Category> findByParentId(UUID parentId) {
        // sortOrder로 정렬된 하위 카테고리 반환
        return jpaRepository.findByParentIdOrderBySortOrder(parentId);
    }

    @Override
    public boolean hasChildren(UUID parentId) {
        return jpaRepository.existsByParent_CategoryIdAndDeletedAtIsNull(parentId);
    }
}
