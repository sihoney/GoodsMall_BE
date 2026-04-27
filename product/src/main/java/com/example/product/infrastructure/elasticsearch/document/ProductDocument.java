package com.example.product.infrastructure.elasticsearch.document;

import com.example.product.domain.entity.Product;
import com.example.product.domain.model.ProductSearchResult;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    private String type;
    private Integer viewCount;
    private String thumbnailS3Key;
    private String createdAt;
    private String updatedAt;

    public ProductDocument() {
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
        this.type = product.getType().name();
        this.viewCount = product.getViewCount();
        this.thumbnailS3Key = thumbnailS3Key;
        this.createdAt = product.getCreatedAt() != null ? product.getCreatedAt().toString() : null;
        this.updatedAt = product.getUpdatedAt() != null ? product.getUpdatedAt().toString() : null;
    }

    public ProductSearchResult toSearchResult() {
        LocalDateTime parsedCreatedAt = createdAt != null
                ? LocalDateTime.parse(createdAt, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                : null;
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
                type,
                viewCount,
                thumbnailS3Key,
                parsedCreatedAt
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

    public String getType() {
        return type;
    }

    public Integer getViewCount() {
        return viewCount;
    }

    public String getThumbnailS3Key() {
        return thumbnailS3Key;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }
}
