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
import java.math.BigDecimal;
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
    public Page<ProductResponse> findDisplayProducts(
            String categoryId,
            String keyword,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Pageable pageable
    ) {
        List<UUID> categoryIds = collectCategoryIds(categoryId);

        // 통합 검색 (카테고리 + 키워드 + 가격 필터 + 정렬)
        Page<Product> products = productRepository.findDisplayProductsWithFilters(
                categoryIds,
                keyword,
                minPrice,
                maxPrice,
                pageable
        );

        return products.map(ProductResponse::from);
    }

    private List<UUID> collectCategoryIds(String categoryId) {
        if (categoryId == null || categoryId.isBlank()) {
            return null;
        }

        UUID categoryUuid = UUID.fromString(categoryId);
        List<UUID> categoryIds = new ArrayList<>();
        categoryIds.add(categoryUuid);
        categoryIds.addAll(categoryRepository.findAllDescendantIds(categoryUuid));
        return categoryIds;
    }

    @Override
    public Page<ProductResponse> findPopularProducts(Pageable pageable) {
        Page<Product> products = productRepository.findPopularProducts(pageable);
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
    @Transactional
    public ProductResponse findById(String productId) {
        Product product = productRepository.findById(UUID.fromString(productId))
                .orElseThrow(ProductNotFoundException::new);
        product.increaseViewCount();
        productRepository.save(product);

        List<ProductImage> images = productImageRepository.findByProductId(UUID.fromString(productId));
        return buildProductResponse(product, images);
    }

    @Override
    public List<ProductAvailabilityResponse> checkAvailability(List<ProductCheckRequest> productRequests) {
        return productRequests.stream()
                .map(this::checkSingleProduct)
                .toList();
    }

    private ProductAvailabilityResponse checkSingleProduct(ProductCheckRequest request) {
        Product product = productRepository.findById(request.productId())
                .orElseThrow(ProductNotFoundException::new);

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
