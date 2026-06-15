package com.example.member.verification.infrastructure.messaging;

import com.example.member.common.infrastructure.messaging.kafka.KafkaTopics;
import com.example.member.verification.application.event.AccountVerificationExpiredEvent;
import com.example.member.verification.application.event.AccountVerificationFailedEvent;
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
public class AccountVerificationEventKafkaProducer {

    private static final String MOCK_TRACE_ID = "mock-trace-id";

    private final KafkaTemplate<String, String> kafkaTemplate;
    // Spring Boot 4 registers the Jackson 3 ObjectMapper under tools.jackson.
    private final ObjectMapper objectMapper;

    public void sendAccountVerificationExpired(AccountVerificationExpiredEvent event) {
        try {
            EventEnvelope<AccountVerificationExpiredEvent> message = new EventEnvelope<>(
                    UUID.randomUUID(),
                    "ACCOUNT_VERIFICATION_EXPIRED",
                    "member-service",
                    event.memberId(),
                    event.memberId(),
                    Instant.now(),
                    MOCK_TRACE_ID,
                    event
            );

            String eventJson = objectMapper.writeValueAsString(message);
            kafkaTemplate.send(
                    KafkaTopics.ACCOUNT_VERIFICATION_EXPIRED,
                    event.memberId().toString(),
                    eventJson
            ).whenComplete((result, exception) -> {
                if (exception != null) {
                    log.error(
                            "Failed to publish EventEnvelope. topic={} memberId={} sessionId={}",
                            KafkaTopics.ACCOUNT_VERIFICATION_EXPIRED,
                            event.memberId(),
                            event.sessionId(),
                            exception
                    );
                    return;
                }

                log.info(
                        "Published EventEnvelope. topic={} partition={} offset={} memberId={} sessionId={}",
                        KafkaTopics.ACCOUNT_VERIFICATION_EXPIRED,
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset(),
                        event.memberId(),
                        event.sessionId()
                );
            });
        } catch (Exception e) {
            log.error(
                    "Failed to serialize EventEnvelope. memberId={} sessionId={}",
                    event.memberId(),
                    event.sessionId(),
                    e
            );
        }
    }

    public void sendAccountVerificationFailed(AccountVerificationFailedEvent event) {
        try {
            EventEnvelope<AccountVerificationFailedEvent> message = new EventEnvelope<>(
                    UUID.randomUUID(),
                    "ACCOUNT_VERIFICATION_FAILED",
                    "member-service",
                    event.memberId(),
                    event.memberId(),
                    Instant.now(),
                    MOCK_TRACE_ID,
                    event
            );

            String eventJson = objectMapper.writeValueAsString(message);
            kafkaTemplate.send(
                    KafkaTopics.ACCOUNT_VERIFICATION_FAILED,
                    event.memberId().toString(),
                    eventJson
            ).whenComplete((result, exception) -> {
                if (exception != null) {
                    log.error(
                            "Failed to publish EventEnvelope. topic={} memberId={} sessionId={}",
                            KafkaTopics.ACCOUNT_VERIFICATION_FAILED,
                            event.memberId(),
                            event.sessionId(),
                            exception
                    );
                    return;
                }

                log.info(
                        "Published EventEnvelope. topic={} partition={} offset={} memberId={} sessionId={}",
                        KafkaTopics.ACCOUNT_VERIFICATION_FAILED,
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset(),
                        event.memberId(),
                        event.sessionId()
                );
            });
        } catch (Exception e) {
            log.error(
                    "Failed to serialize EventEnvelope. memberId={} sessionId={}",
                    event.memberId(),
                    event.sessionId(),
                    e
            );
        }
    }
}
