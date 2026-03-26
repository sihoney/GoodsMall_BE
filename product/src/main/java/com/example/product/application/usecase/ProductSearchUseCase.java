package com.example.product.application.usecase;

import com.example.product.presentation.dto.response.ProductResponse;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductSearchUseCase {

    Page<ProductResponse> findDisplayProducts(Pageable pageable);
    Page<ProductResponse> getAllProducts(Pageable pageable);
    Page<ProductResponse> findBySellerId(String sellerId, Pageable pageable);
    ProductResponse findById(String productId);
}
