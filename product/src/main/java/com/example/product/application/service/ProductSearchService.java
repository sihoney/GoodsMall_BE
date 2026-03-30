package com.example.product.application.service;

import com.example.product.application.usecase.ProductSearchUseCase;
import com.example.product.common.exception.ProductNotFoundException;
import com.example.product.domain.entity.Product;
import com.example.product.domain.entity.ProductImage;
import com.example.product.domain.repository.CategoryRepository;
import com.example.product.domain.repository.ProductImageRepository;
import com.example.product.domain.repository.ProductRepository;
import com.example.product.domain.service.ImageUploadService;
import com.example.product.presentation.dto.request.ProductCheckRequest;
import com.example.product.presentation.dto.response.ProductAvailabilityResponse;
import com.example.product.presentation.dto.response.ProductImageResponse;
import com.example.product.presentation.dto.response.ProductResponse;
import java.util.ArrayList;
import java.util.List;
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
    private final ProductImageRepository productImageRepository;
    private final CategoryRepository categoryRepository;
    private final ImageUploadService imageUploadService;

    @Override
    public Page<ProductResponse> findDisplayProductsByCategory(String categoryId, Pageable pageable) {
        if (categoryId == null || categoryId.isBlank()) {
            Page<Product> products = productRepository.findDisplayProducts(pageable);
            return products.map(ProductResponse::from);
        }

        UUID categoryUuid = UUID.fromString(categoryId);

        List<UUID> categoryIds = new ArrayList<>();
        categoryIds.add(categoryUuid);  // 현재 카테고리 포함
        categoryIds.addAll(categoryRepository.findAllDescendantIds(categoryUuid));  // 하위 카테고리 포함

        Page<Product> products = productRepository.findDisplayProductsByCategoryIds(categoryIds, pageable);
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
        Product product = productRepository.findById(UUID.fromString(productId))
                .orElseThrow(ProductNotFoundException::new);
        List<ProductImage> images = productImageRepository.findByProductId(UUID.fromString(productId));
        return buildProductResponse(product, images);
    }

    @Override
    public List<ProductAvailabilityResponse> checkAvailability(List<ProductCheckRequest> productRequests) {
        if (productRequests == null || productRequests.isEmpty()) {
            throw new IllegalArgumentException("상품 목록은 필수입니다");
        }

        return productRequests.stream()
                .map(this::checkSingleProduct)
                .toList();
    }

    private ProductAvailabilityResponse checkSingleProduct(ProductCheckRequest request) {
        if (request.productId() == null) {
            throw new IllegalArgumentException("상품 ID는 필수입니다");
        }
        if (request.quantity() == null || request.quantity() < 1) {
            throw new IllegalArgumentException("수량은 1개 이상이어야 합니다");
        }

        Product product = productRepository.findById(request.productId())
                .orElseThrow(ProductNotFoundException::new);

        // ProductImage에서 썸네일 조회
        String thumbnailKeySnapshot = productRepository.findThumbnailImageByProductId(request.productId())
                .map(ProductImage::getS3Key)
                .orElse(null);

        return ProductAvailabilityResponse.of(product, request.quantity(), thumbnailKeySnapshot);
    }

    /**
     * ProductResponse 생성 (Presigned URL 포함)
     */
    private ProductResponse buildProductResponse(Product product, List<ProductImage> images) {
        List<ProductImageResponse> imageResponses = images.stream()
                .map(image -> {
                    String presignedUrl = imageUploadService.generatePresignedUrl(image.getS3Key());
                    return ProductImageResponse.from(image, presignedUrl);
                })
                .toList();

        return new ProductResponse(
                product.getProductId(),
                product.getTitle(),
                product.getDescription(),
                product.getPrice(),
                product.getStockQuantity(),
                product.getStatus(),
                product.getCategory().getCategoryId(),
                product.getCategory().getName(),
                product.getCreatedAt(),
                imageResponses
        );
    }
}
