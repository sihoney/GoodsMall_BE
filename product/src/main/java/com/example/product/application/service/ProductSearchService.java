package com.example.product.application.service;

import com.example.product.application.usecase.ProductSearchUseCase;
import com.example.product.common.exception.ProductNotFoundException;
import com.example.product.domain.entity.Product;
import com.example.product.domain.entity.ProductImage;
import com.example.product.domain.enumtype.ProductStatus;
import com.example.product.domain.enumtype.ProductType;
import com.example.product.domain.model.ProductSearchResult;
import com.example.product.domain.repository.CategoryRepository;
import com.example.product.domain.repository.FileStorageRepository;
import com.example.product.domain.repository.ProductImageRepository;
import com.example.product.domain.repository.ProductRepository;
import com.example.product.domain.repository.ProductSearchRepository;
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
    private final FileStorageRepository fileStorageRepository;
    private final ProductSearchRepository productSearchRepository;

    @Override
    public Page<ProductResponse> findDisplayProducts(
            String categoryId,
            String keyword,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Pageable pageable
    ) {
        List<UUID> categoryIds = collectCategoryIds(categoryId);

        return productSearchRepository.searchProducts(categoryIds, keyword, minPrice, maxPrice, pageable)
                .map(this::toProductResponse);
    }

    private ProductResponse toProductResponse(ProductSearchResult result) {
        String presignedUrl = fileStorageRepository.generatePresignedUrl(result.thumbnailS3Key());
        List<ProductImageResponse> images = List.of(ProductImageResponse.ofThumbnail(result.thumbnailS3Key(), presignedUrl));
        return new ProductResponse(
                result.productId(),
                result.title(),
                result.description(),
                result.price(),
                result.stockQuantity(),
                ProductStatus.valueOf(result.status()),
                ProductType.valueOf(result.type()),
                result.categoryId(),
                result.categoryName(),
                result.createdAt(),
                images
        );
    }

    private List<UUID> collectCategoryIds(String categoryId) {
        if (categoryId == null || categoryId.isBlank()) {
            return null;
        }

        UUID categoryUuid = UUID.fromString(categoryId);
        List<UUID> categoryIds = new ArrayList<>();
        categoryIds.add(categoryUuid);
        categoryIds.addAll(categoryRepository.findAllDescendantIds(categoryUuid));

        // 빈 리스트는 null로 변환 (JPQL IN 절 처리를 위해)
        return categoryIds.isEmpty() ? null : categoryIds;
    }

    @Override
    public Page<ProductResponse> findPopularProducts(Pageable pageable) {
        Page<Product> products = productRepository.findPopularProducts(pageable);
        return products.map(product -> {
            List<ProductImage> images = productImageRepository.findByProductId(product.getProductId());
            return buildProductResponse(product, images);
        });
    }

    @Override
    public Page<ProductResponse> getAllProducts(Pageable pageable) {
        Page<Product> products = productRepository.findAll(pageable);
        return products.map(product -> {
            List<ProductImage> images = productImageRepository.findByProductId(product.getProductId());
            return buildProductResponse(product, images);
        });
    }

    @Override
    public Page<ProductResponse> findBySellerId(String sellerId, Pageable pageable) {
        Page<Product> products = productRepository.findBySellerId(UUID.fromString(sellerId), pageable);
        return products.map(product -> {
            List<ProductImage> images = productImageRepository.findByProductId(product.getProductId());
            return buildProductResponse(product, images);
        });
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
    public List<ProductResponse> findByProductIds(List<UUID> productIds) {
        List<Product> products = productRepository.findAllByProductIdIn(productIds);
        return products.stream()
                .map(product -> {
                    List<ProductImage> images = productImageRepository.findByProductId(product.getProductId());
                    return buildProductResponse(product, images);
                })
                .toList();
    }

    /**
     * ProductResponse 생성 (Presigned URL 포함)
     */
    private ProductResponse buildProductResponse(Product product, List<ProductImage> images) {
        List<ProductImageResponse> imageResponses = images.stream()
                .map(image -> {
                    String presignedUrl = fileStorageRepository.generatePresignedUrl(image.getS3Key());
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
                product.getType(),
                product.getCategory().getCategoryId(),
                product.getCategory().getName(),
                product.getCreatedAt(),
                imageResponses
        );
    }
}
