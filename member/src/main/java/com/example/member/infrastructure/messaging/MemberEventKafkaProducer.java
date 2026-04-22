package com.example.member.infrastructure.messaging;

import com.example.member.application.event.MemberSignedUpEvent;
import com.example.member.infrastructure.messaging.kafka.KafkaTopics;
import com.example.member.infrastructure.messaging.kafka.contract.MemberSignedUpPayload;
import tools.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import com.todaylunch.common.event.contract.EventEnvelope;

@Slf4j
@Component
@RequiredArgsConstructor
public class MemberEventKafkaProducer {

    private static final String MOCK_TRACE_ID = "mock-trace-id";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void sendMemberSignedUp(MemberSignedUpEvent event) {
        try {
            EventEnvelope<MemberSignedUpPayload> message = new EventEnvelope<>(
                    event.eventId(),
                    "MEMBER_SIGNED_UP",
                    "member-service",
                    event.memberId(),
                    event.memberId(),
                    event.occurredAt(),
                    MOCK_TRACE_ID,
                    new MemberSignedUpPayload(
                            event.memberId(),
                            event.email()
                    )
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
