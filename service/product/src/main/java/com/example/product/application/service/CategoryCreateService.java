package com.example.product.application.service;

import com.example.product.application.usecase.CategoryCreateUseCase;
import com.example.product.common.exception.SellerCannotCreateRootCategoryException;
import com.example.product.domain.entity.Category;
import com.example.product.domain.repository.CategoryRepository;
import com.example.product.presentation.dto.request.CategoryCreateRequest;
import com.example.product.presentation.dto.response.CategoryResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryCreateService implements CategoryCreateUseCase {

    private final CategoryRepository categoryRepository;

    @Override
    public CategoryResponse createCategory(CategoryCreateRequest request) {
        if (request.parentId() == null) {
            return buildRootCategory(request);
        }
        return buildChildCategoryByAdmin(request);
    }

    @Override
    public CategoryResponse createCategoryBySeller(String sellerId, CategoryCreateRequest request) {
        validateSellerCanCreateCategory(request);

        Category parent = categoryRepository.findById(request.parentId());
        Category category = Category.createChild(
                parent,
                UUID.fromString(sellerId),
                request.name(),
                request.description(),
                request.sortOrder()
        );

        return CategoryResponse.from(categoryRepository.save(category));
    }

    private CategoryResponse buildRootCategory(CategoryCreateRequest request) {
        Category category = Category.createRoot(
                request.name(),
                request.description(),
                request.sortOrder()
        );
        return CategoryResponse.from(categoryRepository.save(category));
    }

    private CategoryResponse buildChildCategoryByAdmin(CategoryCreateRequest request) {
        Category parent = categoryRepository.findById(request.parentId());
        Category category = Category.createChildByAdmin(
                parent,
                request.name(),
                request.description(),
                request.sortOrder()
        );
        return CategoryResponse.from(categoryRepository.save(category));
    }

    private void validateSellerCanCreateCategory(CategoryCreateRequest request) {
        if (request.parentId() == null) {
            throw new SellerCannotCreateRootCategoryException();
        }
    }
}
