package com.example.member.seller.infrastructure.messaging;

import com.example.member.common.infrastructure.messaging.kafka.KafkaTopics;
import com.example.member.seller.application.event.SellerPromotedEvent;
import com.todaylunch.common.event.contract.EventEnvelope;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class SellerEventKafkaProducer {

    private static final String MOCK_TRACE_ID = "mock-trace-id";

    private final KafkaTemplate<String, String> kafkaTemplate;
    // Spring Boot 4 registers the Jackson 3 ObjectMapper under tools.jackson.
    private final ObjectMapper objectMapper;

    public void sendSellerPromoted(SellerPromotedEvent event) {
        try {
            EventEnvelope<SellerPromotedEvent> message = new EventEnvelope<>(
                    UUID.randomUUID(),
                    "SELLER_PROMOTED",
                    "member-service",
                    event.sellerId(),
                    event.memberId(),
                    Instant.now(),
                    MOCK_TRACE_ID,
                    event
            );

            String eventJson = objectMapper.writeValueAsString(message);
            kafkaTemplate.send(
                    KafkaTopics.SELLER_PROMOTED,
                    event.memberId().toString(),
                    eventJson
            ).whenComplete((result, exception) -> {
                if (exception != null) {
                    log.error(
                            "Failed to publish EventEnvelope. topic={} memberId={} sellerId={}",
                            KafkaTopics.SELLER_PROMOTED,
                            event.memberId(),
                            event.sellerId(),
                            exception
                    );
                    return;
                }

                log.info(
                        "Published EventEnvelope. topic={} partition={} offset={} memberId={} sellerId={}",
                        KafkaTopics.SELLER_PROMOTED,
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset(),
                        event.memberId(),
                        event.sellerId()
                );
            });
        } catch (Exception e) {
            log.error(
                    "Failed to serialize EventEnvelope. memberId={} sellerId={}",
                    event.memberId(),
                    event.sellerId(),
                    e
            );
        }
    }
}
