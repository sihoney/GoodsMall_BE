package com.example.member.infrastructure.messaging;

import com.example.member.application.event.AccountVerificationExpiredEvent;
import com.example.member.application.event.AccountVerificationFailedEvent;
import com.example.member.application.event.MemberOauthLinkedEvent;
import com.example.member.application.event.MemberSignedUpEvent;
import com.example.member.application.event.SellerPromotedEvent;
import com.example.member.infrastructure.messaging.kafka.KafkaTopics;
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
