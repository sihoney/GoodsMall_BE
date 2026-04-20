package com.example.member.infrastructure.messaging;

import com.example.member.application.event.MemberSignedUpEvent;
import com.example.member.infrastructure.messaging.kafka.contract.MemberSignedUpPayload;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${member.kafka.topic.signed-up}")
    private String memberSignedUpTopic;

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
                    memberSignedUpTopic,
                    event.memberId().toString(),
                    eventJson
            ).whenComplete((result, exception) -> {
                if (exception != null) {
                    log.error(
                    "Failed to publish EventEnvelope. topic={} memberId={}",
                            memberSignedUpTopic,
                            event.memberId(),
                            exception
                    );
                    return;
                }

                log.info(
                        "Published EventEnvelope. topic={} partition={} offset={} memberId={}",
                        memberSignedUpTopic,
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
