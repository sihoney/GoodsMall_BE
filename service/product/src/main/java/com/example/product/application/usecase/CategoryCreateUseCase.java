package com.example.product.application.usecase;

import com.example.product.presentation.dto.request.CategoryCreateRequest;
import com.example.product.presentation.dto.response.CategoryResponse;

public interface CategoryCreateUseCase {
    CategoryResponse createCategory(CategoryCreateRequest request);

    CategoryResponse createCategoryBySeller(String sellerId, CategoryCreateRequest request);
}
