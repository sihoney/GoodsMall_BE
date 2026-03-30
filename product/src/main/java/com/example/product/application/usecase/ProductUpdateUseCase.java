package com.example.product.application.usecase;

import com.example.product.presentation.dto.request.ProductUpdateRequest;
import com.example.product.presentation.dto.response.ProductResponse;

public interface ProductUpdateUseCase {
    ProductResponse updateProduct(String sellerId, String productId, ProductUpdateRequest request);
    ProductResponse increaseStock(String sellerId, String productId, Integer quantity);
    ProductResponse decreaseStock(String sellerId, String productId, Integer quantity);
}
