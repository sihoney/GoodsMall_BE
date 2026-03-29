package com.example.product.application.service;

import com.example.product.application.usecase.ProductCheckUseCase;
import com.example.product.common.exception.ProductNotFoundException;
import com.example.product.domain.entity.Product;
import com.example.product.domain.repository.ProductRepository;
import com.example.product.presentation.dto.request.ProductCheckRequest;
import com.example.product.presentation.dto.response.ProductAvailabilityResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ProductCheckService implements ProductCheckUseCase {

    private final ProductRepository productRepository;

    @Override
    public List<ProductAvailabilityResponse> checkAvailability(List<ProductCheckRequest> productRequests) {
        if (productRequests == null || productRequests.isEmpty()) {
            throw new IllegalArgumentException("상품 목록은 필수입니다");
        }

        return productRequests.stream()
            .map(request -> checkSingleProduct(request))
            .toList();
    }

    private ProductAvailabilityResponse checkSingleProduct(ProductCheckRequest request) {
        if (request.productId() == null) {
            throw new IllegalArgumentException("상품 ID는 필수입니다");
        }
        if (request.quantity() == null || request.quantity() < 1) {
            throw new IllegalArgumentException("수량은 1개 이상이어야 합니다");
        }

        Product product = productRepository.findById(request.productId());

        // ProductImage에서 썸네일 조회
        String thumbnailKeySnapshot = productRepository.findThumbnailImageByProductId(request.productId())
            .map(image -> image.getS3Key())
            .orElse(null);

        return ProductAvailabilityResponse.of(product, request.quantity(), thumbnailKeySnapshot);
    }
}
