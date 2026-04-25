package com.example.ai.infrastructure.messaging.kafka.dlq;

import com.example.ai.infrastructure.messaging.kafka.InvalidProductEventPayloadException;
import com.example.ai.infrastructure.messaging.kafka.KafkaTopics;
import com.example.ai.infrastructure.messaging.kafka.ProductEventParseException;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaProductEventDlqPublisher implements ProductEventDlqPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void publish(String listenerName, String topic, String rawMessage, Exception exception) {
        ProductEventDlqMessage dlqMessage = new ProductEventDlqMessage(
                listenerName,
                topic,
                resolveReason(exception),
                exception.getClass().getSimpleName(),
                exception.getMessage(),
                rawMessage,
                Instant.now()
        );

        try {
            String payload = objectMapper.writeValueAsString(dlqMessage);
            kafkaTemplate.send(KafkaTopics.PRODUCT_EVENT_DLQ, payload);
        } catch (JacksonException serializationException) {
            log.error(
                    "Failed to serialize AI product event DLQ message. listener={} topic={}",
                    listenerName,
                    topic,
                    serializationException
            );
        } catch (RuntimeException publishException) {
            log.error(
                    "Failed to publish AI product event to DLQ. listener={} topic={} dlqTopic={}",
                    listenerName,
                    topic,
                    KafkaTopics.PRODUCT_EVENT_DLQ,
                    publishException
            );
        }
    }

    private String resolveReason(Exception exception) {
        if (exception instanceof ProductEventParseException) {
            return "PARSE_ERROR";
        }
        if (exception instanceof InvalidProductEventPayloadException) {
            return "INVALID_PAYLOAD";
        }
        return "PROCESSING_ERROR";
    }
}
