package com.example.product.application.service;

import com.example.product.application.usecase.ProductCreateUseCase;
import com.example.product.domain.entity.Product;
import com.example.product.domain.enumtype.ProductStatus;
import com.example.product.domain.repository.ProductRepository;
import com.example.product.presentation.dto.request.ProductCreateRequest;
import com.example.product.presentation.dto.response.ProductResponse;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 상품 등록 Service
 * ProductCreateUseCase를 구현
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ProductCreateService implements ProductCreateUseCase {

    private final ProductRepository productRepository;

    @Override
    public ProductResponse createProduct(ProductCreateRequest request) {
        Product product = Product.create(
                request.sellerId(),
                request.title(),
                request.description(),
                request.price(),
                request.count()
        );
        Product savedProduct = productRepository.save(product);
        return ProductResponse.from(savedProduct);
    }
}
