package com.example.notification.infrastructure.messaging.kafka.dlq;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaNotificationDlqPublisher implements NotificationDlqPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${notification.kafka.topics.dlq:notification.dlq}")
    private String dlqTopic;

    @Override
    public void publish(
            String listenerName,
            String rawMessage,
            RuntimeException exception,
            NotificationConsumerFailureDecision decision
    ) {
        NotificationDlqMessage dlqMessage = new NotificationDlqMessage(
                listenerName,
                decision.reason().name(),
                exception.getClass().getSimpleName(),
                exception.getMessage(),
                rawMessage,
                Instant.now()
        );

        try {
            String payload = objectMapper.writeValueAsString(dlqMessage);
            kafkaTemplate.send(dlqTopic, payload);
        } catch (JsonProcessingException publishPayloadException) {
            log.error(
                    "Failed to serialize DLQ message. listener={} reason={} rawMessage={}",
                    listenerName,
                    decision.reason(),
                    rawMessage,
                    publishPayloadException
            );
        } catch (RuntimeException publishException) {
            log.error(
                    "Failed to publish notification event to DLQ topic. topic={} listener={} reason={} rawMessage={}",
                    dlqTopic,
                    listenerName,
                    decision.reason(),
                    rawMessage,
                    publishException
            );
        }
    }
}
