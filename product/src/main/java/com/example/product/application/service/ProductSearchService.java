package com.example.product.application.service;

import com.example.product.application.usecase.ProductSearchUseCase;
import com.example.product.domain.entity.Product;
import com.example.product.domain.repository.ProductRepository;
import com.example.product.presentation.dto.response.ProductResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductSearchService implements ProductSearchUseCase {

    private final ProductRepository productRepository;

    @Override
    public Page<ProductResponse> findDisplayProducts(Pageable pageable) {
        Page<Product> products = productRepository.findDisplayProducts(pageable);
        return products.map(ProductResponse::from);
    }

    @Override
    public Page<ProductResponse> getAllProducts(Pageable pageable) {
        Page<Product> products = productRepository.findAll(pageable);
        return products.map(ProductResponse::from);
    }

    @Override
    public Page<ProductResponse> findBySellerId(String sellerId, Pageable pageable) {
        Page<Product> products = productRepository.findBySellerId(UUID.fromString(sellerId), pageable);
        return products.map(ProductResponse::from);
    }
    @Override
    public ProductResponse findById(String productId) {
        return ProductResponse.from(productRepository.findById(UUID.fromString(productId)));
    }
}
