package com.example.product.domain.repository;

import com.example.product.domain.entity.Product;

public interface ProductRepository {
    Product save(Product product);
}
