package com.example.product.application.service;

import com.example.product.application.usecase.ProductDeleteUseCase;
import com.example.product.common.exception.ProductNotFoundException;
import com.example.product.domain.entity.Product;
import com.example.product.domain.repository.ProductRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductDeleteService implements ProductDeleteUseCase {

    private final ProductRepository productRepository;

    @Override
    public void deleteProduct(String sellerId, String productId) {
        Product product = productRepository.findById(UUID.fromString(productId))
                .orElseThrow(ProductNotFoundException::new);

        product.validateSeller(UUID.fromString(sellerId));

        product.delete();
    }
}
