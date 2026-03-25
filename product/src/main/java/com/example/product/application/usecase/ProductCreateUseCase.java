package com.example.product.application.usecase;

import com.example.product.presentation.dto.request.ProductCreateRequest;
import com.example.product.presentation.dto.response.ProductResponse;

public interface ProductCreateUseCase {
    ProductResponse createProduct(ProductCreateRequest request);
}
