package com.example.member.auth.infrastructure.messaging;

import com.example.member.auth.application.event.MemberOauthLinkedEvent;
import com.example.member.common.infrastructure.messaging.kafka.KafkaTopics;
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
public class MemberOauthEventKafkaProducer {

    private static final String MOCK_TRACE_ID = "mock-trace-id";

    private final KafkaTemplate<String, String> kafkaTemplate;
    // Spring Boot 4 registers the Jackson 3 ObjectMapper under tools.jackson.
    private final ObjectMapper objectMapper;

    public void sendMemberOauthLinked(MemberOauthLinkedEvent event) {
        try {
            EventEnvelope<MemberOauthLinkedEvent> message = new EventEnvelope<>(
                    UUID.randomUUID(),
                    "MEMBER_OAUTH_LINKED",
                    "member-service",
                    event.memberId(),
                    event.memberId(),
                    Instant.now(),
                    MOCK_TRACE_ID,
                    event
            );

            String eventJson = objectMapper.writeValueAsString(message);
            kafkaTemplate.send(
                    KafkaTopics.MEMBER_OAUTH_LINKED,
                    event.memberId().toString(),
                    eventJson
            ).whenComplete((result, exception) -> {
                if (exception != null) {
                    log.error(
                            "Failed to publish EventEnvelope. topic={} memberId={} provider={} providerUserId={}",
                            KafkaTopics.MEMBER_OAUTH_LINKED,
                            event.memberId(),
                            event.provider(),
                            event.providerUserId(),
                            exception
                    );
                    return;
                }

                log.info(
                        "Published EventEnvelope. topic={} partition={} offset={} memberId={} provider={} providerUserId={}",
                        KafkaTopics.MEMBER_OAUTH_LINKED,
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset(),
                        event.memberId(),
                        event.provider(),
                        event.providerUserId()
                );
            });
        } catch (Exception e) {
            log.error(
                    "Failed to serialize EventEnvelope. memberId={} provider={} providerUserId={}",
                    event.memberId(),
                    event.provider(),
                    event.providerUserId(),
                    e
            );
        }
    }
}
