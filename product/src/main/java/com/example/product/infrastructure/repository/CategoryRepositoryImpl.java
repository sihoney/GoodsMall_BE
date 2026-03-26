package com.example.product.infrastructure.repository;

import com.example.product.domain.entity.Category;
import com.example.product.domain.repository.CategoryRepository;
import com.example.product.presentation.exception.CategoryNotFoundException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CategoryRepositoryImpl implements CategoryRepository {
    private final CategoryJpaRepository jpaRepository;

    @Override
    public Category findById(UUID categoryId) {
        return jpaRepository.findById(categoryId).orElseThrow(CategoryNotFoundException::new);
    }
}
