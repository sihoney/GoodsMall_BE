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
        return jpaRepository.findAllByDeletedAtIsNull();
    }

    @Override
    public Category findById(UUID categoryId) {
        return jpaRepository.findByCategoryIdAndDeletedAtIsNull(categoryId)
                .orElseThrow(CategoryNotFoundException::new);
    }

    @Override
    public List<Category> findByDepth(Integer depth) {
        return jpaRepository.findByDepthAndDeletedAtIsNull(depth);
    }

    @Override
    public List<Category> findByParentCategory(Category category) {
        return jpaRepository.findByParentAndDeletedAtIsNull(category);
    }

    @Override
    public boolean hasChildren(UUID categoryId) {
        return jpaRepository.existsByParent_CategoryIdAndDeletedAtIsNull(categoryId);
    }
}
