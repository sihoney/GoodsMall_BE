package com.example.product.infrastructure.repository;

import com.example.product.domain.entity.Category;
import com.example.product.domain.repository.CategoryRepository;
import com.example.product.common.exception.CategoryNotFoundException;
import java.util.ArrayList;
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
        return jpaRepository.findByParentIdOrderBySortOrder(parentId);
    }

    @Override
    public boolean hasChildren(UUID parentId) {
        return jpaRepository.existsByParent_CategoryIdAndDeletedAtIsNull(parentId);
    }

    @Override
    public List<UUID> findAllDescendantIds(UUID categoryId) {
        List<UUID> descendantIds = new ArrayList<>();
        collectDescendantIds(categoryId, descendantIds);
        return descendantIds;
    }

    private void collectDescendantIds(UUID parentId, List<UUID> result) {
        List<Category> children = findByParentId(parentId);
        for (Category child : children) {
            result.add(child.getCategoryId());
            collectDescendantIds(child.getCategoryId(), result);
        }
    }
}
