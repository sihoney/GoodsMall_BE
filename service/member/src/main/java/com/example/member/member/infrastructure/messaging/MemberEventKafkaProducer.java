package com.example.member.member.infrastructure.messaging;

import com.example.member.common.infrastructure.messaging.kafka.KafkaTopics;
import com.example.member.member.application.event.MemberSignedUpEvent;
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
public class MemberEventKafkaProducer {

    private static final String MOCK_TRACE_ID = "mock-trace-id";

    private final KafkaTemplate<String, String> kafkaTemplate;
    // Spring Boot 4 registers the Jackson 3 ObjectMapper under tools.jackson.
    private final ObjectMapper objectMapper;

    public void sendMemberSignedUp(MemberSignedUpEvent event) {
        try {
            EventEnvelope<MemberSignedUpEvent> message = new EventEnvelope<>(
                    UUID.randomUUID(),
                    "MEMBER_SIGNED_UP",
                    "member-service",
                    event.memberId(),
                    event.memberId(),
                    Instant.now(),
                    MOCK_TRACE_ID,
                    event
            );

            String eventJson = objectMapper.writeValueAsString(message);
            kafkaTemplate.send(
                    KafkaTopics.MEMBER_SIGNED_UP,
                    event.memberId().toString(),
                    eventJson
            ).whenComplete((result, exception) -> {
                if (exception != null) {
                    log.error(
                            "Failed to publish EventEnvelope. topic={} memberId={}",
                            KafkaTopics.MEMBER_SIGNED_UP,
                            event.memberId(),
                            exception
                    );
                    return;
                }

                log.info(
                        "Published EventEnvelope. topic={} partition={} offset={} memberId={}",
                        KafkaTopics.MEMBER_SIGNED_UP,
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset(),
                        event.memberId()
                );
            });
        } catch (Exception e) {
            log.error(
                    "Failed to serialize EventEnvelope. memberId={}",
                    event.memberId(),
                    e
            );
        }
    }
}
