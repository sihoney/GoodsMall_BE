package com.example.product.infrastructure.elasticsearch.document;

import com.example.product.domain.entity.Product;
import com.example.product.domain.model.ProductSearchResult;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Document(indexName = "products", createIndex = false)
public class ProductDocument {

    @Id
    private String productId;

    @Field(type = FieldType.Keyword)
    private String sellerId;

    @Field(type = FieldType.Keyword)
    private String categoryId;

    @Field(type = FieldType.Keyword)
    private List<String> categoryIds;

    @Field(type = FieldType.Keyword)
    private String categoryName;

    @Field(type = FieldType.Text)
    private String title;

    @Field(type = FieldType.Text)
    private String description;

    @Field(type = FieldType.Double)
    private BigDecimal price;

    @Field(type = FieldType.Integer)
    private Integer stockQuantity;

    @Field(type = FieldType.Keyword)
    private String status;

    @Field(type = FieldType.Keyword)
    private String type;

    @Field(type = FieldType.Integer)
    private Integer viewCount;

    @Field(type = FieldType.Keyword)
    private String thumbnailS3Key;

    @Field(type = FieldType.Keyword)
    private String createdAt;

    @Field(type = FieldType.Keyword)
    private String updatedAt;

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
        this.type = product.getType().name();
        this.viewCount = product.getViewCount();
        this.thumbnailS3Key = thumbnailS3Key;
        this.createdAt = product.getCreatedAt() != null ? product.getCreatedAt().toString() : null;
        this.updatedAt = product.getUpdatedAt() != null ? product.getUpdatedAt().toString() : null;
    }

    public ProductSearchResult toSearchResult() {
        LocalDateTime parsedCreatedAt = parseDate(createdAt);
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

    // ES date 타입은 "2024-01-15T10:30:00" 또는 "2024-01-15T10:30:00.000Z" 등 다양한 포맷으로 반환될 수 있음
    private static LocalDateTime parseDate(String s) {
        if (s == null) return null;
        try {
            return LocalDateTime.parse(s);
        } catch (DateTimeParseException e) {
            return OffsetDateTime.parse(s).toLocalDateTime();
        }
    }
}
