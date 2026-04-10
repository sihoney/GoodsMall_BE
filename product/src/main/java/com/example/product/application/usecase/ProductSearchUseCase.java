package com.example.product.application.usecase;

import com.example.product.presentation.dto.response.ProductResponse;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductSearchUseCase {

    Page<ProductResponse> findDisplayProducts(
            String categoryId,
            String keyword,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Pageable pageable
    );

    Page<ProductResponse> findPopularProducts(Pageable pageable);

    Page<ProductResponse> getAllProducts(Pageable pageable);
    Page<ProductResponse> findBySellerId(String sellerId, Pageable pageable);
    ProductResponse findById(String productId);
    List<ProductResponse> findByProductIds(List<UUID> productIds);
}
