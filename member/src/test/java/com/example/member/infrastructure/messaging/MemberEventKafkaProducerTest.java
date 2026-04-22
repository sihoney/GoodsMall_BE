package com.example.member.infrastructure.messaging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.member.infrastructure.messaging.kafka.contract.AccountVerificationExpiredPayload;
import com.example.member.infrastructure.messaging.kafka.contract.AccountVerificationFailedPayload;
import com.example.member.infrastructure.messaging.kafka.contract.MemberOauthLinkedPayload;
import com.example.member.infrastructure.messaging.kafka.contract.MemberSignedUpPayload;
import com.example.member.infrastructure.messaging.kafka.contract.SellerPromotedPayload;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import com.todaylunch.common.event.contract.EventEnvelope;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.SendResult;

@ExtendWith(MockitoExtension.class)
class MemberEventKafkaProducerTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private org.springframework.kafka.core.KafkaTemplate<String, String> kafkaTemplate;

    @InjectMocks
    private MemberEventKafkaProducer memberEventKafkaProducer;

    @Test
    void sendMemberSignedUp_serializesContractMessage() throws Exception {
        UUID memberId = UUID.randomUUID();
        MemberSignedUpPayload payload = new MemberSignedUpPayload(memberId, "member@test.com");

        when(objectMapper.writeValueAsString(any())).thenReturn("{\"ok\":true}");

        ProducerRecord<String, String> record = new ProducerRecord<>("member-signed-up", memberId.toString(), "{\"ok\":true}");
        RecordMetadata metadata = new RecordMetadata(
                new TopicPartition("member-signed-up", 0),
                0L,
                0,
                System.currentTimeMillis(),
                0,
                0
        );
        SendResult<String, String> sendResult = new SendResult<>(record, metadata);
        when(kafkaTemplate.send(anyString(), anyString(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(sendResult));

        memberEventKafkaProducer.sendMemberSignedUp(payload);

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
        assertInstanceOf(MemberSignedUpPayload.class, message.payload());
        MemberSignedUpPayload capturedPayload = (MemberSignedUpPayload) message.payload();
        assertEquals(memberId, capturedPayload.memberId());
        assertEquals("member@test.com", capturedPayload.email());
    }

    @Test
    void sendSellerPromoted_serializesContractMessage() throws Exception {
        UUID memberId = UUID.randomUUID();
        UUID sellerId = UUID.randomUUID();
        SellerPromotedPayload payload = new SellerPromotedPayload(memberId, sellerId, "KAKAO");

        when(objectMapper.writeValueAsString(any())).thenReturn("{\"ok\":true}");

        ProducerRecord<String, String> record = new ProducerRecord<>("member-seller-promoted", memberId.toString(), "{\"ok\":true}");
        RecordMetadata metadata = new RecordMetadata(
                new TopicPartition("member-seller-promoted", 0),
                0L,
                0,
                System.currentTimeMillis(),
                0,
                0
        );
        SendResult<String, String> sendResult = new SendResult<>(record, metadata);
        when(kafkaTemplate.send(anyString(), anyString(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(sendResult));

        memberEventKafkaProducer.sendSellerPromoted(payload);

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
        assertInstanceOf(SellerPromotedPayload.class, message.payload());
        SellerPromotedPayload capturedPayload = (SellerPromotedPayload) message.payload();
        assertEquals(memberId, capturedPayload.memberId());
        assertEquals(sellerId, capturedPayload.sellerId());
        assertEquals("KAKAO", capturedPayload.bankName());
    }

    @Test
    void sendAccountVerificationExpired_serializesContractMessage() throws Exception {
        UUID memberId = UUID.randomUUID();
        AccountVerificationExpiredPayload payload = new AccountVerificationExpiredPayload(memberId, "av_expired", "SESSION_EXPIRED");

        when(objectMapper.writeValueAsString(any())).thenReturn("{\"ok\":true}");

        ProducerRecord<String, String> record = new ProducerRecord<>("member-account-verification-expired", memberId.toString(), "{\"ok\":true}");
        RecordMetadata metadata = new RecordMetadata(
                new TopicPartition("member-account-verification-expired", 0),
                0L,
                0,
                System.currentTimeMillis(),
                0,
                0
        );
        SendResult<String, String> sendResult = new SendResult<>(record, metadata);
        when(kafkaTemplate.send(anyString(), anyString(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(sendResult));

        memberEventKafkaProducer.sendAccountVerificationExpired(payload);

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
        assertInstanceOf(AccountVerificationExpiredPayload.class, message.payload());
        AccountVerificationExpiredPayload capturedPayload = (AccountVerificationExpiredPayload) message.payload();
        assertEquals(memberId, capturedPayload.memberId());
        assertEquals("av_expired", capturedPayload.sessionId());
        assertEquals("SESSION_EXPIRED", capturedPayload.reason());
    }

    @Test
    void sendAccountVerificationFailed_serializesContractMessage() throws Exception {
        UUID memberId = UUID.randomUUID();
        AccountVerificationFailedPayload payload = new AccountVerificationFailedPayload(memberId, "av_failed", "ATTEMPT_LIMIT_EXCEEDED");

        when(objectMapper.writeValueAsString(any())).thenReturn("{\"ok\":true}");

        ProducerRecord<String, String> record = new ProducerRecord<>("member-account-verification-failed", memberId.toString(), "{\"ok\":true}");
        RecordMetadata metadata = new RecordMetadata(
                new TopicPartition("member-account-verification-failed", 0),
                0L,
                0,
                System.currentTimeMillis(),
                0,
                0
        );
        SendResult<String, String> sendResult = new SendResult<>(record, metadata);
        when(kafkaTemplate.send(anyString(), anyString(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(sendResult));

        memberEventKafkaProducer.sendAccountVerificationFailed(payload);

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
        assertInstanceOf(AccountVerificationFailedPayload.class, message.payload());
        AccountVerificationFailedPayload capturedPayload = (AccountVerificationFailedPayload) message.payload();
        assertEquals(memberId, capturedPayload.memberId());
        assertEquals("av_failed", capturedPayload.sessionId());
        assertEquals("ATTEMPT_LIMIT_EXCEEDED", capturedPayload.reason());
    }

    @Test
    void sendMemberOauthLinked_serializesContractMessage() throws Exception {
        UUID memberId = UUID.randomUUID();
        LocalDateTime linkedAt = LocalDateTime.of(2026, 4, 22, 12, 0);
        MemberOauthLinkedPayload payload = new MemberOauthLinkedPayload(
                memberId,
                "KAKAO",
                "provider-user-1",
                "member@test.com",
                "tester",
                linkedAt
        );

        when(objectMapper.writeValueAsString(any())).thenReturn("{\"ok\":true}");

        ProducerRecord<String, String> record = new ProducerRecord<>("member-oauth-linked", memberId.toString(), "{\"ok\":true}");
        RecordMetadata metadata = new RecordMetadata(
                new TopicPartition("member-oauth-linked", 0),
                0L,
                0,
                System.currentTimeMillis(),
                0,
                0
        );
        SendResult<String, String> sendResult = new SendResult<>(record, metadata);
        when(kafkaTemplate.send(anyString(), anyString(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(sendResult));

        memberEventKafkaProducer.sendMemberOauthLinked(payload);

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
        assertInstanceOf(MemberOauthLinkedPayload.class, message.payload());
        MemberOauthLinkedPayload capturedPayload = (MemberOauthLinkedPayload) message.payload();
        assertEquals(memberId, capturedPayload.memberId());
        assertEquals("KAKAO", capturedPayload.provider());
        assertEquals("provider-user-1", capturedPayload.providerUserId());
        assertEquals("member@test.com", capturedPayload.providerEmail());
        assertEquals("tester", capturedPayload.providerNickname());
        assertEquals(linkedAt, capturedPayload.linkedAt());
    }
}
