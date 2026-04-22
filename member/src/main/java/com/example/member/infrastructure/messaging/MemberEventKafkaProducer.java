package com.example.member.infrastructure.messaging;

import com.example.member.infrastructure.messaging.kafka.KafkaTopics;
import com.example.member.infrastructure.messaging.kafka.contract.AccountVerificationExpiredPayload;
import com.example.member.infrastructure.messaging.kafka.contract.AccountVerificationFailedPayload;
import com.example.member.infrastructure.messaging.kafka.contract.MemberSignedUpPayload;
import com.example.member.infrastructure.messaging.kafka.contract.MemberOauthLinkedPayload;
import com.example.member.infrastructure.messaging.kafka.contract.SellerPromotedPayload;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.UUID;
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

    public void sendMemberSignedUp(MemberSignedUpPayload payload) {
        try {
            EventEnvelope<MemberSignedUpPayload> message = new EventEnvelope<>(
                    UUID.randomUUID(),
                    "MEMBER_SIGNED_UP",
                    "member-service",
                    payload.memberId(),
                    payload.memberId(),
                    Instant.now(),
                    MOCK_TRACE_ID,
                    payload
            );

            String eventJson = objectMapper.writeValueAsString(message);
            kafkaTemplate.send(
                    KafkaTopics.MEMBER_SIGNED_UP,
                    payload.memberId().toString(),
                    eventJson
            ).whenComplete((result, exception) -> {
                if (exception != null) {
                    log.error(
                    "Failed to publish EventEnvelope. topic={} memberId={}",
                            KafkaTopics.MEMBER_SIGNED_UP,
                            payload.memberId(),
                            exception
                    );
                    return;
                }

                log.info(
                        "Published EventEnvelope. topic={} partition={} offset={} memberId={}",
                        KafkaTopics.MEMBER_SIGNED_UP,
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset(),
                        payload.memberId()
                );
            });
        } catch (Exception e) {
            log.error(
                    "Failed to serialize EventEnvelope. memberId={}",
                    payload.memberId(),
                    e
            );
        }
    }

    public void sendSellerPromoted(SellerPromotedPayload payload) {
        try {
            EventEnvelope<SellerPromotedPayload> message = new EventEnvelope<>(
                    UUID.randomUUID(),
                    "SELLER_PROMOTED",
                    "member-service",
                    payload.sellerId(),
                    payload.memberId(),
                    Instant.now(),
                    MOCK_TRACE_ID,
                    payload
            );

            String eventJson = objectMapper.writeValueAsString(message);
            kafkaTemplate.send(
                    KafkaTopics.SELLER_PROMOTED,
                    payload.memberId().toString(),
                    eventJson
            ).whenComplete((result, exception) -> {
                if (exception != null) {
                    log.error(
                            "Failed to publish EventEnvelope. topic={} memberId={} sellerId={}",
                            KafkaTopics.SELLER_PROMOTED,
                            payload.memberId(),
                            payload.sellerId(),
                            exception
                    );
                    return;
                }

                log.info(
                        "Published EventEnvelope. topic={} partition={} offset={} memberId={} sellerId={}",
                        KafkaTopics.SELLER_PROMOTED,
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset(),
                        payload.memberId(),
                        payload.sellerId()
                );
            });
        } catch (Exception e) {
            log.error(
                    "Failed to serialize EventEnvelope. memberId={} sellerId={}",
                    payload.memberId(),
                    payload.sellerId(),
                    e
            );
        }
    }

    public void sendAccountVerificationExpired(AccountVerificationExpiredPayload payload) {
        try {
            EventEnvelope<AccountVerificationExpiredPayload> message = new EventEnvelope<>(
                    UUID.randomUUID(),
                    "ACCOUNT_VERIFICATION_EXPIRED",
                    "member-service",
                    payload.memberId(),
                    payload.memberId(),
                    Instant.now(),
                    MOCK_TRACE_ID,
                    payload
            );

            String eventJson = objectMapper.writeValueAsString(message);
            kafkaTemplate.send(
                    KafkaTopics.ACCOUNT_VERIFICATION_EXPIRED,
                    payload.memberId().toString(),
                    eventJson
            ).whenComplete((result, exception) -> {
                if (exception != null) {
                    log.error(
                            "Failed to publish EventEnvelope. topic={} memberId={} sessionId={}",
                            KafkaTopics.ACCOUNT_VERIFICATION_EXPIRED,
                            payload.memberId(),
                            payload.sessionId(),
                            exception
                    );
                    return;
                }

                log.info(
                        "Published EventEnvelope. topic={} partition={} offset={} memberId={} sessionId={}",
                        KafkaTopics.ACCOUNT_VERIFICATION_EXPIRED,
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset(),
                        payload.memberId(),
                        payload.sessionId()
                );
            });
        } catch (Exception e) {
            log.error(
                    "Failed to serialize EventEnvelope. memberId={} sessionId={}",
                    payload.memberId(),
                    payload.sessionId(),
                    e
            );
        }
    }

    public void sendAccountVerificationFailed(AccountVerificationFailedPayload payload) {
        try {
            EventEnvelope<AccountVerificationFailedPayload> message = new EventEnvelope<>(
                    UUID.randomUUID(),
                    "ACCOUNT_VERIFICATION_FAILED",
                    "member-service",
                    payload.memberId(),
                    payload.memberId(),
                    Instant.now(),
                    MOCK_TRACE_ID,
                    payload
            );

            String eventJson = objectMapper.writeValueAsString(message);
            kafkaTemplate.send(
                    KafkaTopics.ACCOUNT_VERIFICATION_FAILED,
                    payload.memberId().toString(),
                    eventJson
            ).whenComplete((result, exception) -> {
                if (exception != null) {
                    log.error(
                            "Failed to publish EventEnvelope. topic={} memberId={} sessionId={}",
                            KafkaTopics.ACCOUNT_VERIFICATION_FAILED,
                            payload.memberId(),
                            payload.sessionId(),
                            exception
                    );
                    return;
                }

                log.info(
                        "Published EventEnvelope. topic={} partition={} offset={} memberId={} sessionId={}",
                        KafkaTopics.ACCOUNT_VERIFICATION_FAILED,
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset(),
                        payload.memberId(),
                        payload.sessionId()
                );
            });
        } catch (Exception e) {
            log.error(
                    "Failed to serialize EventEnvelope. memberId={} sessionId={}",
                    payload.memberId(),
                    payload.sessionId(),
                    e
            );
        }
    }

    public void sendMemberOauthLinked(MemberOauthLinkedPayload payload) {
        try {
            EventEnvelope<MemberOauthLinkedPayload> message = new EventEnvelope<>(
                    UUID.randomUUID(),
                    "MEMBER_OAUTH_LINKED",
                    "member-service",
                    payload.memberId(),
                    payload.memberId(),
                    Instant.now(),
                    MOCK_TRACE_ID,
                    payload
            );

            String eventJson = objectMapper.writeValueAsString(message);
            kafkaTemplate.send(
                    KafkaTopics.MEMBER_OAUTH_LINKED,
                    payload.memberId().toString(),
                    eventJson
            ).whenComplete((result, exception) -> {
                if (exception != null) {
                    log.error(
                            "Failed to publish EventEnvelope. topic={} memberId={} provider={} providerUserId={}",
                            KafkaTopics.MEMBER_OAUTH_LINKED,
                            payload.memberId(),
                            payload.provider(),
                            payload.providerUserId(),
                            exception
                    );
                    return;
                }

                log.info(
                        "Published EventEnvelope. topic={} partition={} offset={} memberId={} provider={} providerUserId={}",
                        KafkaTopics.MEMBER_OAUTH_LINKED,
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset(),
                        payload.memberId(),
                        payload.provider(),
                        payload.providerUserId()
                );
            });
        } catch (Exception e) {
            log.error(
                    "Failed to serialize EventEnvelope. memberId={} provider={} providerUserId={}",
                    payload.memberId(),
                    payload.provider(),
                    payload.providerUserId(),
                    e
            );
        }
    }
}
