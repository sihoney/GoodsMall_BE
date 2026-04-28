package com.example.product.infrastructure.messaging.kafka;

import com.example.product.application.event.OutboxEventPendingTrigger;
import com.example.product.domain.entity.OutboxEvent;
import com.example.product.domain.entity.Product;
import com.example.product.domain.repository.OutboxEventRepository;
import com.example.product.infrastructure.messaging.kafka.message.ProductCreatedMessage;
import com.example.product.infrastructure.messaging.kafka.message.ProductDeletedMessage;
import com.example.product.infrastructure.messaging.kafka.message.ProductUpdatedMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductOutboxEventService {

    private static final String AGGREGATE_TYPE_PRODUCT = "PRODUCT";

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher eventPublisher;

    public void saveCreatedEvent(Product product) {
        String sourceUpdatedAt = formatNullable(product.getUpdatedAt());
        String now = Instant.now().toString();
        ProductCreatedMessage message = new ProductCreatedMessage(
                UUID.randomUUID().toString(),
                product.getProductId().toString(),
                product.getTitle(),
                product.getTitle(),
                product.getCategory().getName(),
                product.getDescription(),
                product.getStatus().name(),
                sourceUpdatedAt,
                now,
                now
        );

        saveOutboxEvent(product.getProductId(),
                        ProductEventTypes.PRODUCT_CREATED,
                        KafkaTopics.PRODUCT_CREATED,
                        serialize(message));
    }

    public void saveUpdatedEvent(Product product) {
        String sourceUpdatedAt = formatNullable(product.getUpdatedAt());
        String now = Instant.now().toString();
        ProductUpdatedMessage message = new ProductUpdatedMessage(
                UUID.randomUUID().toString(),
                product.getProductId().toString(),
                product.getTitle(),
                product.getTitle(),
                product.getCategory().getName(),
                product.getDescription(),
                product.getStatus().name(),
                sourceUpdatedAt,
                now,
                now
        );

        saveOutboxEvent(product.getProductId(),
                        ProductEventTypes.PRODUCT_UPDATED,
                        KafkaTopics.PRODUCT_UPDATED,
                        serialize(message));
    }

    public void saveDeletedEvent(Product product) {
        String sourceUpdatedAt = formatNullable(product.getUpdatedAt());
        String now = Instant.now().toString();
        ProductDeletedMessage message = new ProductDeletedMessage(
                UUID.randomUUID().toString(),
                product.getProductId().toString(),
                sourceUpdatedAt,
                now,
                now
        );

        saveOutboxEvent(product.getProductId(),
                        ProductEventTypes.PRODUCT_DELETED,
                        KafkaTopics.PRODUCT_DELETED,
                        serialize(message));
    }

    private void saveOutboxEvent(
            UUID aggregateId,
            String eventType,
            String topic,
            String payload
    ) {
        outboxEventRepository.save(OutboxEvent.create(
                aggregateId,
                AGGREGATE_TYPE_PRODUCT,
                eventType,
                topic,
                aggregateId.toString(),
                payload
        ));
        eventPublisher.publishEvent(new OutboxEventPendingTrigger());
    }

    private String serialize(Object message) {
        try {
            return objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Product outbox 이벤트 직렬화에 실패했습니다.", e);
        }
    }

    private String formatNullable(LocalDateTime time) {
        return time != null ? time.toString() : null;
    }
}
