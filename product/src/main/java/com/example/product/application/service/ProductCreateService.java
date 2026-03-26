package com.example.product.application.service;

import com.example.product.application.usecase.ProductCreateUseCase;
import com.example.product.domain.entity.Category;
import com.example.product.domain.entity.Product;
import com.example.product.domain.repository.CategoryRepository;
import com.example.product.domain.repository.ProductRepository;
import com.example.product.presentation.dto.request.ProductCreateRequest;
import com.example.product.presentation.dto.response.ProductResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 상품 등록 Service ProductCreateUseCase를 구현
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ProductCreateService implements ProductCreateUseCase {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;


    @Override
    public ProductResponse createProduct(String sellerId, ProductCreateRequest request) {
        Product product = Product.create(
            sellerId,
            request.title(),
            request.description(),
            request.price(),
            request.stockQuantity(),
            categoryRepository.findById(request.categoryId())
        );

        Product savedProduct = productRepository.save(product);
        return ProductResponse.from(savedProduct);
    }
}
