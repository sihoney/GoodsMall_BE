package com.example.ai.infrastructure.messaging.kafka;

import com.example.ai.application.dto.ProductDeactivateCommand;
import com.example.ai.application.dto.ProductEmbeddingCommand;
import com.example.ai.application.usecase.ProductEmbeddingUseCase;
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
    private static final String UPSERT_ACTION = "upsert";
    private static final String DEACTIVATE_ACTION = "deactivate";

    private final ProductEmbeddingUseCase productEmbeddingUseCase;
    private final EventIdempotencyRepository eventIdempotencyRepository;
    private final ObjectMapper objectMapper;

    @Value("${ai.event.idempotency-ttl-seconds:259200}")
    private long idempotencyTtlSeconds;

    @KafkaListener(
            topics = KafkaTopics.PRODUCT_CREATED,
            groupId = KafkaConsumerGroups.AI_PRODUCT_EMBEDDING_GROUP,
            containerFactory = "productEventKafkaListenerContainerFactory"
    )
    public void consumeProductCreated(String payload) {
        ProductCreatedMessage message = parse(payload, ProductCreatedMessage.class);
        validateUpsertPayload(
                message.productId(),
                message.sourceUpdatedAt(),
                message.updatedAt(),
                message.occurredAt(),
                firstNonBlank(message.productName(), message.title()),
                message.categoryName(),
                message.description()
        );
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
            groupId = KafkaConsumerGroups.AI_PRODUCT_EMBEDDING_GROUP,
            containerFactory = "productEventKafkaListenerContainerFactory"
    )
    public void consumeProductUpdated(String payload) {
        ProductUpdatedMessage message = parse(payload, ProductUpdatedMessage.class);
        validateUpsertPayload(
                message.productId(),
                message.sourceUpdatedAt(),
                message.updatedAt(),
                message.occurredAt(),
                firstNonBlank(message.productName(), message.title()),
                message.categoryName(),
                message.description()
        );
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
            groupId = KafkaConsumerGroups.AI_PRODUCT_EMBEDDING_GROUP,
            containerFactory = "productEventKafkaListenerContainerFactory"
    )
    public void consumeProductDeleted(String payload) {
        ProductDeletedMessage message = parse(payload, ProductDeletedMessage.class);
        validateDeactivatePayload(
                message.productId(),
                message.sourceUpdatedAt(),
                message.updatedAt(),
                message.occurredAt()
        );
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
        String key = buildIdempotencyKey(eventId, productId, sourceUpdatedAt, UPSERT_ACTION);

        if (!eventIdempotencyRepository.reserve(key, Duration.ofSeconds(idempotencyTtlSeconds))) {
            log.info(
                    "Skip duplicated product event. action={} productId={} eventId={} sourceUpdatedAt={} key={}",
                    UPSERT_ACTION,
                    productId,
                    eventId,
                    sourceUpdatedAt,
                    key
            );
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
            log.info(
                    "Product upsert event processed. action={} productId={} eventId={} sourceUpdatedAt={}",
                    UPSERT_ACTION,
                    productId,
                    eventId,
                    sourceUpdatedAt
            );
        } catch (RuntimeException e) {
            eventIdempotencyRepository.release(key);
            log.warn(
                    "Product upsert event failed after idempotency reservation. Released key for retry. action={} productId={} eventId={} sourceUpdatedAt={} key={}",
                    UPSERT_ACTION,
                    productId,
                    eventId,
                    sourceUpdatedAt,
                    key
            );
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
        String key = buildIdempotencyKey(eventId, productId, sourceUpdatedAt, DEACTIVATE_ACTION);

        if (!eventIdempotencyRepository.reserve(key, Duration.ofSeconds(idempotencyTtlSeconds))) {
            log.info(
                    "Skip duplicated product deactivate event. action={} productId={} eventId={} sourceUpdatedAt={} key={}",
                    DEACTIVATE_ACTION,
                    productId,
                    eventId,
                    sourceUpdatedAt,
                    key
            );
            return;
        }

        try {
            productEmbeddingUseCase.deactivate(new ProductDeactivateCommand(productId, sourceUpdatedAt));
            log.info(
                    "Product deactivate event processed. action={} productId={} eventId={} sourceUpdatedAt={}",
                    DEACTIVATE_ACTION,
                    productId,
                    eventId,
                    sourceUpdatedAt
            );
        } catch (RuntimeException e) {
            eventIdempotencyRepository.release(key);
            log.warn(
                    "Product deactivate event failed after idempotency reservation. Released key for retry. action={} productId={} eventId={} sourceUpdatedAt={} key={}",
                    DEACTIVATE_ACTION,
                    productId,
                    eventId,
                    sourceUpdatedAt,
                    key
            );
            throw e;
        }
    }

    private <T> T parse(String payload, Class<T> targetType) {
        try {
            return objectMapper.readValue(payload, targetType);
        } catch (Exception e) {
            throw new ProductEventParseException("Product 이벤트 역직렬화에 실패했습니다.", e);
        }
    }

    private UUID parseRequiredUuid(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new InvalidProductEventPayloadException(fieldName + "는 필수입니다.");
        }
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            throw new InvalidProductEventPayloadException(fieldName + " 형식이 올바르지 않습니다.", e);
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
                throw new InvalidProductEventPayloadException("sourceUpdatedAt 파싱에 실패했습니다.", e);
            }
        }
    }

    private void validateUpsertPayload(
            String productId,
            String sourceUpdatedAt,
            String updatedAt,
            String occurredAt,
            String productName,
            String categoryName,
            String description
    ) {
        validateRequiredSourceUpdatedAt(productId, sourceUpdatedAt, updatedAt, occurredAt);
        if (firstNonBlank(productName, categoryName, description) == null) {
            throw new InvalidProductEventPayloadException("임베딩 입력 텍스트가 비어 있습니다.");
        }
    }

    private void validateDeactivatePayload(
            String productId,
            String sourceUpdatedAt,
            String updatedAt,
            String occurredAt
    ) {
        validateRequiredSourceUpdatedAt(productId, sourceUpdatedAt, updatedAt, occurredAt);
    }

    private void validateRequiredSourceUpdatedAt(
            String productId,
            String sourceUpdatedAt,
            String updatedAt,
            String occurredAt
    ) {
        if (productId == null || productId.isBlank()) {
            throw new InvalidProductEventPayloadException("productId는 필수입니다.");
        }
        if (firstNonBlank(sourceUpdatedAt, updatedAt, occurredAt) == null) {
            throw new InvalidProductEventPayloadException("sourceUpdatedAt, updatedAt, occurredAt 중 하나는 필수입니다.");
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

