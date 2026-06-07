package com.example.member.common.infrastructure.messaging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.member.verification.application.event.AccountVerificationExpiredEvent;
import com.example.member.verification.application.event.AccountVerificationFailedEvent;
import com.example.member.auth.application.event.MemberOauthLinkedEvent;
import com.example.member.auth.infrastructure.messaging.MemberOauthEventKafkaProducer;
import com.example.member.member.application.event.MemberSignedUpEvent;
import com.example.member.member.infrastructure.messaging.MemberEventKafkaProducer;
import com.example.member.seller.application.event.SellerPromotedEvent;
import com.example.member.seller.infrastructure.messaging.SellerEventKafkaProducer;
import com.example.member.verification.infrastructure.messaging.AccountVerificationEventKafkaProducer;
import com.todaylunch.common.event.contract.EventEnvelope;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.SendResult;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class MemberEventKafkaProducerTest {

    @Mock
    // Matches the Jackson 3 ObjectMapper used by the member runtime configuration.
    private ObjectMapper objectMapper;

    @Mock
    private org.springframework.kafka.core.KafkaTemplate<String, String> kafkaTemplate;

    @Test
    void sendMemberSignedUp_serializesContractMessage() throws Exception {
        UUID memberId = UUID.randomUUID();
        MemberSignedUpEvent payload = new MemberSignedUpEvent(memberId, "member@test.com");

        when(objectMapper.writeValueAsString(any())).thenReturn("{\"ok\":true}");

        ProducerRecord<String, String> record = new ProducerRecord<>("member.signed-up", memberId.toString(), "{\"ok\":true}");
        RecordMetadata metadata = new RecordMetadata(
                new TopicPartition("member.signed-up", 0),
                0L,
                0,
                System.currentTimeMillis(),
                0,
                0
        );
        SendResult<String, String> sendResult = new SendResult<>(record, metadata);
        when(kafkaTemplate.send(anyString(), anyString(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(sendResult));

        MemberEventKafkaProducer producer = new MemberEventKafkaProducer(kafkaTemplate, objectMapper);
        producer.sendMemberSignedUp(payload);

        ArgumentCaptor<Object> messageCaptor = ArgumentCaptor.forClass(Object.class);
        verify(objectMapper).writeValueAsString(messageCaptor.capture());

        Object captured = messageCaptor.getValue();
        assertInstanceOf(EventEnvelope.class, captured);
        EventEnvelope<?> message = (EventEnvelope<?>) captured;
        assertEquals("MEMBER_SIGNED_UP", message.eventType());
        assertEquals("member-service", message.source());
        assertEquals(memberId, message.aggregateId());
        assertEquals(memberId, message.recipientId());
        assertEquals("mock-trace-id", message.traceId());
        org.junit.jupiter.api.Assertions.assertNotNull(message.eventId());
        org.junit.jupiter.api.Assertions.assertNotNull(message.occurredAt());
        assertInstanceOf(MemberSignedUpEvent.class, message.payload());
        MemberSignedUpEvent capturedPayload = (MemberSignedUpEvent) message.payload();
        assertEquals(memberId, capturedPayload.memberId());
        assertEquals("member@test.com", capturedPayload.email());
    }

    @Test
    void sendSellerPromoted_serializesContractMessage() throws Exception {
        UUID memberId = UUID.randomUUID();
        UUID sellerId = UUID.randomUUID();
        SellerPromotedEvent payload = new SellerPromotedEvent(memberId, sellerId, "KAKAO");

        when(objectMapper.writeValueAsString(any())).thenReturn("{\"ok\":true}");

        ProducerRecord<String, String> record = new ProducerRecord<>("member.seller-promoted", memberId.toString(), "{\"ok\":true}");
        RecordMetadata metadata = new RecordMetadata(
                new TopicPartition("member.seller-promoted", 0),
                0L,
                0,
                System.currentTimeMillis(),
                0,
                0
        );
        SendResult<String, String> sendResult = new SendResult<>(record, metadata);
        when(kafkaTemplate.send(anyString(), anyString(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(sendResult));

        SellerEventKafkaProducer producer = new SellerEventKafkaProducer(kafkaTemplate, objectMapper);
        producer.sendSellerPromoted(payload);

        ArgumentCaptor<Object> messageCaptor = ArgumentCaptor.forClass(Object.class);
        verify(objectMapper).writeValueAsString(messageCaptor.capture());

        Object captured = messageCaptor.getValue();
        assertInstanceOf(EventEnvelope.class, captured);
        EventEnvelope<?> message = (EventEnvelope<?>) captured;
        assertEquals("SELLER_PROMOTED", message.eventType());
        assertEquals("member-service", message.source());
        assertEquals(sellerId, message.aggregateId());
        assertEquals(memberId, message.recipientId());
        assertEquals("mock-trace-id", message.traceId());
        org.junit.jupiter.api.Assertions.assertNotNull(message.eventId());
        org.junit.jupiter.api.Assertions.assertNotNull(message.occurredAt());
        assertInstanceOf(SellerPromotedEvent.class, message.payload());
        SellerPromotedEvent capturedPayload = (SellerPromotedEvent) message.payload();
        assertEquals(memberId, capturedPayload.memberId());
        assertEquals(sellerId, capturedPayload.sellerId());
        assertEquals("KAKAO", capturedPayload.bankName());
    }

    @Test
    void sendAccountVerificationExpired_serializesContractMessage() throws Exception {
        UUID memberId = UUID.randomUUID();
        AccountVerificationExpiredEvent payload = new AccountVerificationExpiredEvent(memberId, "av_expired", "SESSION_EXPIRED");

        when(objectMapper.writeValueAsString(any())).thenReturn("{\"ok\":true}");

        ProducerRecord<String, String> record = new ProducerRecord<>("member.account-verification-expired", memberId.toString(), "{\"ok\":true}");
        RecordMetadata metadata = new RecordMetadata(
                new TopicPartition("member.account-verification-expired", 0),
                0L,
                0,
                System.currentTimeMillis(),
                0,
                0
        );
        SendResult<String, String> sendResult = new SendResult<>(record, metadata);
        when(kafkaTemplate.send(anyString(), anyString(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(sendResult));

        AccountVerificationEventKafkaProducer producer =
                new AccountVerificationEventKafkaProducer(kafkaTemplate, objectMapper);
        producer.sendAccountVerificationExpired(payload);

        ArgumentCaptor<Object> messageCaptor = ArgumentCaptor.forClass(Object.class);
        verify(objectMapper).writeValueAsString(messageCaptor.capture());

        EventEnvelope<?> message = (EventEnvelope<?>) messageCaptor.getValue();
        assertEquals("ACCOUNT_VERIFICATION_EXPIRED", message.eventType());
        assertEquals("member-service", message.source());
        assertEquals(memberId, message.aggregateId());
        assertEquals(memberId, message.recipientId());
        assertEquals("mock-trace-id", message.traceId());
        org.junit.jupiter.api.Assertions.assertNotNull(message.eventId());
        org.junit.jupiter.api.Assertions.assertNotNull(message.occurredAt());
        assertInstanceOf(AccountVerificationExpiredEvent.class, message.payload());
        AccountVerificationExpiredEvent capturedPayload = (AccountVerificationExpiredEvent) message.payload();
        assertEquals(memberId, capturedPayload.memberId());
        assertEquals("av_expired", capturedPayload.sessionId());
        assertEquals("SESSION_EXPIRED", capturedPayload.reason());
    }

    @Test
    void sendAccountVerificationFailed_serializesContractMessage() throws Exception {
        UUID memberId = UUID.randomUUID();
        AccountVerificationFailedEvent payload = new AccountVerificationFailedEvent(memberId, "av_failed", "ATTEMPT_LIMIT_EXCEEDED");

        when(objectMapper.writeValueAsString(any())).thenReturn("{\"ok\":true}");

        ProducerRecord<String, String> record = new ProducerRecord<>("member.account-verification-failed", memberId.toString(), "{\"ok\":true}");
        RecordMetadata metadata = new RecordMetadata(
                new TopicPartition("member.account-verification-failed", 0),
                0L,
                0,
                System.currentTimeMillis(),
                0,
                0
        );
        SendResult<String, String> sendResult = new SendResult<>(record, metadata);
        when(kafkaTemplate.send(anyString(), anyString(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(sendResult));

        AccountVerificationEventKafkaProducer producer =
                new AccountVerificationEventKafkaProducer(kafkaTemplate, objectMapper);
        producer.sendAccountVerificationFailed(payload);

        ArgumentCaptor<Object> messageCaptor = ArgumentCaptor.forClass(Object.class);
        verify(objectMapper).writeValueAsString(messageCaptor.capture());

        EventEnvelope<?> message = (EventEnvelope<?>) messageCaptor.getValue();
        assertEquals("ACCOUNT_VERIFICATION_FAILED", message.eventType());
        assertEquals("member-service", message.source());
        assertEquals(memberId, message.aggregateId());
        assertEquals(memberId, message.recipientId());
        assertEquals("mock-trace-id", message.traceId());
        org.junit.jupiter.api.Assertions.assertNotNull(message.eventId());
        org.junit.jupiter.api.Assertions.assertNotNull(message.occurredAt());
        assertInstanceOf(AccountVerificationFailedEvent.class, message.payload());
        AccountVerificationFailedEvent capturedPayload = (AccountVerificationFailedEvent) message.payload();
        assertEquals(memberId, capturedPayload.memberId());
        assertEquals("av_failed", capturedPayload.sessionId());
        assertEquals("ATTEMPT_LIMIT_EXCEEDED", capturedPayload.reason());
    }

    @Test
    void sendMemberOauthLinked_serializesContractMessage() throws Exception {
        UUID memberId = UUID.randomUUID();
        LocalDateTime linkedAt = LocalDateTime.of(2026, 4, 22, 12, 0);
        MemberOauthLinkedEvent payload = new MemberOauthLinkedEvent(
                memberId,
                "KAKAO",
                "provider-user-1",
                "member@test.com",
                "tester",
                linkedAt
        );

        when(objectMapper.writeValueAsString(any())).thenReturn("{\"ok\":true}");

        ProducerRecord<String, String> record = new ProducerRecord<>("member.oauth-linked", memberId.toString(), "{\"ok\":true}");
        RecordMetadata metadata = new RecordMetadata(
                new TopicPartition("member.oauth-linked", 0),
                0L,
                0,
                System.currentTimeMillis(),
                0,
                0
        );
        SendResult<String, String> sendResult = new SendResult<>(record, metadata);
        when(kafkaTemplate.send(anyString(), anyString(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(sendResult));

        MemberOauthEventKafkaProducer producer = new MemberOauthEventKafkaProducer(kafkaTemplate, objectMapper);
        producer.sendMemberOauthLinked(payload);

        ArgumentCaptor<Object> messageCaptor = ArgumentCaptor.forClass(Object.class);
        verify(objectMapper).writeValueAsString(messageCaptor.capture());

        EventEnvelope<?> message = (EventEnvelope<?>) messageCaptor.getValue();
        assertEquals("MEMBER_OAUTH_LINKED", message.eventType());
        assertEquals("member-service", message.source());
        assertEquals(memberId, message.aggregateId());
        assertEquals(memberId, message.recipientId());
        assertEquals("mock-trace-id", message.traceId());
        org.junit.jupiter.api.Assertions.assertNotNull(message.eventId());
        org.junit.jupiter.api.Assertions.assertNotNull(message.occurredAt());
        assertInstanceOf(MemberOauthLinkedEvent.class, message.payload());
        MemberOauthLinkedEvent capturedPayload = (MemberOauthLinkedEvent) message.payload();
        assertEquals(memberId, capturedPayload.memberId());
        assertEquals("KAKAO", capturedPayload.provider());
        assertEquals("provider-user-1", capturedPayload.providerUserId());
        assertEquals("member@test.com", capturedPayload.providerEmail());
        assertEquals("tester", capturedPayload.providerNickname());
        assertEquals(linkedAt, capturedPayload.linkedAt());
    }
}
