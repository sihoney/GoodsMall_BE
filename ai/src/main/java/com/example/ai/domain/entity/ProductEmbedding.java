package com.example.ai.domain.entity;

import com.example.ai.common.exception.AiEmbeddingException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnTransformer;

@Getter
@Entity
@Table(name = "product_embedding", schema = "ai")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductEmbedding {

    @Id
    @Column(name = "embedding_id", nullable = false, updatable = false)
    private UUID embeddingId;

    @Column(name = "product_id", nullable = false, unique = true)
    private UUID productId;

    @Column(name = "embedding", nullable = false, columnDefinition = "vector(1536)")
    @ColumnTransformer(write = "?::vector", read = "embedding::text")
    private String embedding;

    @Column(name = "source_updated_at", nullable = false)
    private LocalDateTime sourceUpdatedAt;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    private ProductEmbedding(UUID productId, List<Float> embeddingVector, LocalDateTime sourceUpdatedAt) {
        this.embeddingId = UUID.randomUUID();
        this.productId = productId;
        this.embedding = toVectorLiteral(embeddingVector);
        this.sourceUpdatedAt = sourceUpdatedAt;
        this.active = true;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public static ProductEmbedding create(UUID productId, List<Float> embeddingVector, LocalDateTime sourceUpdatedAt) {
        if (productId == null) {
            throw new AiEmbeddingException("productId는 비어 있을 수 없습니다.");
        }
        if (embeddingVector == null || embeddingVector.isEmpty()) {
            throw new AiEmbeddingException("임베딩 벡터가 비어 있습니다.");
        }
        if (sourceUpdatedAt == null) {
            throw new AiEmbeddingException("sourceUpdatedAt은 필수입니다.");
        }
        return new ProductEmbedding(productId, List.copyOf(embeddingVector), sourceUpdatedAt);
    }

    public void updateEmbedding(List<Float> embeddingVector, LocalDateTime sourceUpdatedAt) {
        if (embeddingVector == null || embeddingVector.isEmpty()) {
            throw new AiEmbeddingException("임베딩 벡터가 비어 있습니다.");
        }
        if (sourceUpdatedAt == null) {
            throw new AiEmbeddingException("sourceUpdatedAt은 필수입니다.");
        }
        this.embedding = toVectorLiteral(embeddingVector);
        this.sourceUpdatedAt = sourceUpdatedAt;
        this.active = true;
        this.updatedAt = LocalDateTime.now();
    }

    public void deactivate(LocalDateTime sourceUpdatedAt) {
        if (sourceUpdatedAt == null) {
            throw new AiEmbeddingException("sourceUpdatedAt은 필수입니다.");
        }
        this.active = false;
        this.sourceUpdatedAt = sourceUpdatedAt;
        this.updatedAt = LocalDateTime.now();
    }

    private static String toVectorLiteral(List<Float> vector) {
        StringBuilder builder = new StringBuilder();
        builder.append('[');
        for (int i = 0; i < vector.size(); i++) {
            if (i > 0) {
                builder.append(',');
            }
            builder.append(vector.get(i));
        }
        builder.append(']');
        return builder.toString();
    }
}
