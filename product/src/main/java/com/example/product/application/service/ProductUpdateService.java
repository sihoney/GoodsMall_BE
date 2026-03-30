package com.example.product.application.service;

import com.example.product.application.usecase.ProductUpdateUseCase;
import com.example.product.common.exception.ProductNotFoundException;
import com.example.product.domain.entity.Category;
import com.example.product.domain.entity.Product;
import com.example.product.domain.repository.CategoryRepository;
import com.example.product.domain.repository.ProductRepository;
import com.example.product.presentation.dto.request.ProductUpdateRequest;
import com.example.product.presentation.dto.response.ProductResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductUpdateService implements ProductUpdateUseCase {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public ProductResponse updateProduct(String sellerId, String productId, ProductUpdateRequest request) {
        Product product = productRepository.findById(UUID.fromString(productId))
                .orElseThrow(ProductNotFoundException::new);

        product.validateSeller(UUID.fromString(sellerId));

        Category category = categoryRepository.findById(request.categoryId());

        product.updateProductInfo(request.title(), request.description(), request.price());
        product.updateCategory(category);
        product.updateStock(request.stockQuantity());

        return ProductResponse.from(product);
    }
}
