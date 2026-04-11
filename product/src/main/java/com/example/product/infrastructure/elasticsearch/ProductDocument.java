package com.example.product.infrastructure.elasticsearch;

import com.example.product.domain.entity.Product;
import com.example.product.domain.model.ProductSearchResult;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductDocument {

    private String productId;
    private String sellerId;
    private String categoryId;
    private List<String> categoryIds;
    private String categoryName;
    private String title;
    private String description;
    private BigDecimal price;
    private Integer stockQuantity;
    private String status;
    private Integer viewCount;
    private String thumbnailS3Key;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    protected ProductDocument() {
    }

    public ProductDocument(Product product, List<String> allCategoryIds, String thumbnailS3Key) {
        this.productId = product.getProductId().toString();
        this.sellerId = product.getSellerId().toString();
        this.categoryId = product.getCategory().getCategoryId().toString();
        this.categoryIds = allCategoryIds;
        this.categoryName = product.getCategory().getName();
        this.title = product.getTitle();
        this.description = product.getDescription();
        this.price = product.getPrice();
        this.stockQuantity = product.getStockQuantity();
        this.status = product.getStatus().name();
        this.viewCount = product.getViewCount();
        this.thumbnailS3Key = thumbnailS3Key;
        this.createdAt = product.getCreatedAt();
        this.updatedAt = product.getUpdatedAt();
    }

    public ProductSearchResult toSearchResult() {
        return new ProductSearchResult(
                UUID.fromString(productId),
                UUID.fromString(sellerId),
                UUID.fromString(categoryId),
                categoryName,
                title,
                description,
                price,
                stockQuantity,
                status,
                viewCount,
                thumbnailS3Key,
                createdAt
        );
    }

    public String getProductId() {
        return productId;
    }

    public String getSellerId() {
        return sellerId;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public List<String> getCategoryIds() {
        return categoryIds;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Integer getStockQuantity() {
        return stockQuantity;
    }

    public String getStatus() {
        return status;
    }

    public Integer getViewCount() {
        return viewCount;
    }

    public String getThumbnailS3Key() {
        return thumbnailS3Key;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
