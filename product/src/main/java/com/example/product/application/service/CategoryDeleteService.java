package com.example.product.application.service;

import com.example.product.application.usecase.CategoryDeleteUseCase;
import com.example.product.common.exception.CategoryHasChildrenException;
import com.example.product.domain.entity.Category;
import com.example.product.domain.repository.CategoryRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryDeleteService implements CategoryDeleteUseCase {

    private final CategoryRepository categoryRepository;

    @Override
    public void deleteCategory(UUID categoryId) {
        if (categoryRepository.hasChildren(categoryId)) {
            throw new CategoryHasChildrenException();
        }

        Category category = categoryRepository.findById(categoryId);
        category.delete();
    }
}
