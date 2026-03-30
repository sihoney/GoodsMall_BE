package com.example.product.application.usecase;

import com.example.product.presentation.dto.request.ProductCheckRequest;
import com.example.product.presentation.dto.response.ProductAvailabilityResponse;
import com.example.product.presentation.dto.response.ProductResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductSearchUseCase {

    Page<ProductResponse> findDisplayProductsByCategory(String categoryId, Pageable pageable);
    Page<ProductResponse> getAllProducts(Pageable pageable);
    Page<ProductResponse> findBySellerId(String sellerId, Pageable pageable);
    ProductResponse findById(String productId);
    List<ProductAvailabilityResponse> checkAvailability(List<ProductCheckRequest> productRequests);
}
