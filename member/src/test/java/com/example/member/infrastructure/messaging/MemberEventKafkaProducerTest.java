package com.example.member.infrastructure.messaging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.member.application.event.MemberSignedUpEvent;
import com.example.member.infrastructure.messaging.kafka.contract.MemberSignedUpPayload;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
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
        UUID eventId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        Instant occurredAt = Instant.parse("2026-04-16T10:00:00Z");
        MemberSignedUpEvent event = new MemberSignedUpEvent(eventId, memberId, "member@test.com", occurredAt);

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

        memberEventKafkaProducer.sendMemberSignedUp(event);

        ArgumentCaptor<Object> messageCaptor = ArgumentCaptor.forClass(Object.class);
        verify(objectMapper).writeValueAsString(messageCaptor.capture());

        Object captured = messageCaptor.getValue();
        assertInstanceOf(EventEnvelope.class, captured);
        EventEnvelope<?> message = (EventEnvelope<?>) captured;
        assertEquals(eventId, message.eventId());
        assertEquals("MEMBER_SIGNED_UP", message.eventType());
        assertEquals("member-service", message.source());
        assertEquals(memberId, message.aggregateId());
        assertEquals(memberId, message.recipientId());
        assertEquals(occurredAt, message.occurredAt());
        assertEquals("mock-trace-id", message.traceId());
        assertInstanceOf(MemberSignedUpPayload.class, message.payload());
        MemberSignedUpPayload payload = (MemberSignedUpPayload) message.payload();
        assertEquals(memberId, payload.memberId());
        assertEquals("member@test.com", payload.email());
    }
}
