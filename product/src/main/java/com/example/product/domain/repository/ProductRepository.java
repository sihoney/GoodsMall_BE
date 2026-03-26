package com.example.product.domain.repository;

import com.example.product.domain.entity.Product;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductRepository {

    Product save(Product product);

    Page<Product> findAll(Pageable pageable);

    Page<Product> findDisplayProducts(Pageable pageable);

    Page<Product> findBySellerId(UUID sellerId, Pageable pageable);

    Product findById(UUID productId);


}
