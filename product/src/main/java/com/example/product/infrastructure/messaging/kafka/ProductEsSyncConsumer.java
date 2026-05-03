package com.example.product.infrastructure.messaging.kafka;

import com.example.product.common.exception.ProductNotFoundException;
import com.example.product.domain.entity.Product;
import com.example.product.domain.entity.ProductImage;
import com.example.product.domain.repository.ProductImageRepository;
import com.example.product.domain.repository.ProductRepository;
import com.example.product.domain.repository.ProductSearchRepository;
import com.example.product.infrastructure.messaging.kafka.message.ProductCreatedMessage;
import com.example.product.infrastructure.messaging.kafka.message.ProductDeletedMessage;
import com.example.product.infrastructure.messaging.kafka.message.ProductUpdatedMessage;
import tools.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductEsSyncConsumer {

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductSearchRepository productSearchRepository;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    @KafkaListener(topics = KafkaTopics.PRODUCT_CREATED)
    public void handleProductCreated(String payload) {
        try {
            ProductCreatedMessage message = objectMapper.readValue(payload, ProductCreatedMessage.class);
            indexProduct(UUID.fromString(message.productId()));
        } catch (Exception e) {
            log.error("상품 생성 ES 인덱싱 실패", e);
        }
    }

    @Transactional(readOnly = true)
    @KafkaListener(topics = KafkaTopics.PRODUCT_UPDATED)
    public void handleProductUpdated(String payload) {
        try {
            ProductUpdatedMessage message = objectMapper.readValue(payload, ProductUpdatedMessage.class);
            indexProduct(UUID.fromString(message.productId()));
        } catch (Exception e) {
            log.error("상품 수정 ES 인덱싱 실패", e);
        }
    }

    @KafkaListener(topics = KafkaTopics.PRODUCT_DELETED)
    public void handleProductDeleted(String payload) {
        try {
            ProductDeletedMessage message = objectMapper.readValue(payload, ProductDeletedMessage.class);
            productSearchRepository.delete(UUID.fromString(message.productId()));
        } catch (Exception e) {
            log.error("상품 삭제 ES 인덱스 삭제 실패", e);
        }
    }

    private void indexProduct(UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(ProductNotFoundException::new);

        String thumbnailS3Key = productImageRepository.findThumbnailByProductId(productId)
                .map(ProductImage::getS3Key)
                .orElse(null);

        List<String> categoryIds = product.getCategory().collectIdHierarchy().stream()
                .map(UUID::toString)
                .toList();

        productSearchRepository.index(product, categoryIds, thumbnailS3Key);
        log.debug("상품 ES 인덱싱 완료: productId={}", productId);
    }
}
