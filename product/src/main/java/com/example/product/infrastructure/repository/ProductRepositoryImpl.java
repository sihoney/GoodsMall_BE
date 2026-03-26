package com.example.product.infrastructure.repository;

import com.example.product.domain.entity.Product;
import com.example.product.domain.repository.ProductRepository;
import com.example.product.common.exception.ProductNotFoundException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepository {

    private final ProductJpaRepository jpaRepository;

    @Override
    public Product save(Product product) {
        return jpaRepository.save(product);
    }

    @Override
    public Page<Product> findAll(Pageable pageable) {
        return jpaRepository.findAll(pageable);
    }

    @Override
    public Page<Product> findDisplayProducts(Pageable pageable) {
        return jpaRepository.findDisplayProducts(pageable);
    }

    @Override
    public Page<Product> findBySellerId(UUID sellerId, Pageable pageable) {
        return jpaRepository.findBySellerId(sellerId, pageable);
    }

    @Override
    public Product findById(UUID productId) {
        return  jpaRepository.findById(productId).orElseThrow(ProductNotFoundException::new);
    }
}
