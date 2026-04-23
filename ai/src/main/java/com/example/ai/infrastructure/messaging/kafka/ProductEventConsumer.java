package com.example.ai.infrastructure.messaging.kafka;

import com.example.ai.application.dto.ProductDeactivateCommand;
import com.example.ai.application.dto.ProductEmbeddingCommand;
import com.example.ai.application.usecase.ProductEmbeddingUseCase;
import com.example.ai.common.exception.AiEmbeddingException;
import com.example.ai.infrastructure.embedding.EventIdempotencyRepository;
import com.example.ai.infrastructure.messaging.kafka.contract.ProductCreatedMessage;
import com.example.ai.infrastructure.messaging.kafka.contract.ProductDeletedMessage;
import com.example.ai.infrastructure.messaging.kafka.contract.ProductUpdatedMessage;
import tools.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductEventConsumer {

    // TODO: 토픽 이름과 내용은 발행에 따라 변경이 될 수 있습니다.
    private static final String IDEMPOTENCY_PREFIX = "ai:event:product:";

    private final ProductEmbeddingUseCase productEmbeddingUseCase;
    private final EventIdempotencyRepository eventIdempotencyRepository;
    private final ObjectMapper objectMapper;

    @Value("${ai.event.idempotency-ttl-seconds:259200}")
    private long idempotencyTtlSeconds;

    @KafkaListener(
            topics = KafkaTopics.PRODUCT_CREATED,
            groupId = KafkaConsumerGroups.AI_PRODUCT_EMBEDDING_GROUP
    )
    public void consumeProductCreated(String payload) {
        ProductCreatedMessage message = parse(payload, ProductCreatedMessage.class);
        processUpsertEvent(
                message.eventId(),
                message.productId(),
                message.sourceUpdatedAt(),
                message.updatedAt(),
                message.occurredAt(),
                firstNonBlank(message.productName(), message.title()),
                message.categoryName(),
                message.description(),
                message.status()
        );
    }

    @KafkaListener(
            topics = KafkaTopics.PRODUCT_UPDATED,
            groupId = KafkaConsumerGroups.AI_PRODUCT_EMBEDDING_GROUP
    )
    public void consumeProductUpdated(String payload) {
        ProductUpdatedMessage message = parse(payload, ProductUpdatedMessage.class);
        processUpsertEvent(
                message.eventId(),
                message.productId(),
                message.sourceUpdatedAt(),
                message.updatedAt(),
                message.occurredAt(),
                firstNonBlank(message.productName(), message.title()),
                message.categoryName(),
                message.description(),
                message.status()
        );
    }

    @KafkaListener(
            topics = KafkaTopics.PRODUCT_DELETED,
            groupId = KafkaConsumerGroups.AI_PRODUCT_EMBEDDING_GROUP
    )
    public void consumeProductDeleted(String payload) {
        ProductDeletedMessage message = parse(payload, ProductDeletedMessage.class);
        processDeactivateEvent(
                message.eventId(),
                message.productId(),
                message.sourceUpdatedAt(),
                message.updatedAt(),
                message.occurredAt()
        );
    }

    private void processUpsertEvent(
            String eventId,
            String productIdText,
            String sourceUpdatedAtText,
            String updatedAtText,
            String occurredAtText,
            String productName,
            String categoryName,
            String description,
            String status
    ) {
        UUID productId = parseRequiredUuid(productIdText, "productId");
        LocalDateTime sourceUpdatedAt = parseSourceUpdatedAt(sourceUpdatedAtText, updatedAtText, occurredAtText);
        String key = buildIdempotencyKey(eventId, productId, sourceUpdatedAt, "upsert");

        if (!eventIdempotencyRepository.reserve(key, Duration.ofSeconds(idempotencyTtlSeconds))) {
            log.info("Skip duplicated product event: key={}", key);
            return;
        }

        try {
            if (isInactiveStatus(status)) {
                productEmbeddingUseCase.deactivate(new ProductDeactivateCommand(productId, sourceUpdatedAt));
                log.info("Product inactive event processed: productId={}", productId);
                return;
            }

            productEmbeddingUseCase.embedding(new ProductEmbeddingCommand(
                    productId,
                    productName,
                    categoryName,
                    description,
                    sourceUpdatedAt
            ));
            log.info("Product upsert event processed: productId={}", productId);
        } catch (RuntimeException e) {
            eventIdempotencyRepository.release(key);
            throw e;
        }
    }

    private void processDeactivateEvent(
            String eventId,
            String productIdText,
            String sourceUpdatedAtText,
            String updatedAtText,
            String occurredAtText
    ) {
        UUID productId = parseRequiredUuid(productIdText, "productId");
        LocalDateTime sourceUpdatedAt = parseSourceUpdatedAt(sourceUpdatedAtText, updatedAtText, occurredAtText);
        String key = buildIdempotencyKey(eventId, productId, sourceUpdatedAt, "deactivate");

        if (!eventIdempotencyRepository.reserve(key, Duration.ofSeconds(idempotencyTtlSeconds))) {
            log.info("Skip duplicated product delete event: key={}", key);
            return;
        }

        try {
            productEmbeddingUseCase.deactivate(new ProductDeactivateCommand(productId, sourceUpdatedAt));
            log.info("Product delete event processed: productId={}", productId);
        } catch (RuntimeException e) {
            eventIdempotencyRepository.release(key);
            throw e;
        }
    }

    private <T> T parse(String payload, Class<T> targetType) {
        try {
            return objectMapper.readValue(payload, targetType);
        } catch (Exception e) {
            throw new AiEmbeddingException("Product 이벤트 역직렬화에 실패했습니다.", e);
        }
    }

    private UUID parseRequiredUuid(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new AiEmbeddingException(fieldName + "는 필수입니다.");
        }
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            throw new AiEmbeddingException(fieldName + " 형식이 올바르지 않습니다.", e);
        }
    }

    private LocalDateTime parseSourceUpdatedAt(String sourceUpdatedAt, String updatedAt, String occurredAt) {
        // TODO: Fallback order may change when publisher timestamp semantics are finalized.
        String value = firstNonBlank(sourceUpdatedAt, updatedAt, occurredAt);
        if (value == null) {
            return LocalDateTime.now();
        }

        try {
            return LocalDateTime.parse(value);
        } catch (DateTimeParseException ignore) {
            try {
                return LocalDateTime.ofInstant(Instant.parse(value), ZoneOffset.UTC);
            } catch (DateTimeParseException e) {
                throw new AiEmbeddingException("sourceUpdatedAt 파싱에 실패했습니다.", e);
            }
        }
    }

    private String buildIdempotencyKey(String eventId, UUID productId, LocalDateTime sourceUpdatedAt, String action) {
        if (eventId != null && !eventId.isBlank()) {
            return IDEMPOTENCY_PREFIX + eventId;
        }
        return IDEMPOTENCY_PREFIX + productId + ":" + action + ":" + sourceUpdatedAt;
    }

    private boolean isInactiveStatus(String status) {
        return "INACTIVE".equalsIgnoreCase(status);
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }
}

