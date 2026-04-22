package com.example.payment.infrastructure.messaging.kafka;

import com.example.payment.application.dto.CreateWalletCommand;
import com.example.payment.application.usecase.CreateWalletUseCase;
import com.example.payment.common.exception.InvalidChargeRequestException;
import com.example.payment.infrastructure.messaging.kafka.contract.MemberSignedUpPayload;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.todaylunch.common.event.contract.EventEnvelope;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MemberCreatedEventConsumer {

    private static final String MEMBER_SIGNED_UP_EVENT_TYPE = "MEMBER_SIGNED_UP";
    private static final ZoneId KOREA_ZONE_ID = ZoneId.of("Asia/Seoul");
    private static final TypeReference<EventEnvelope<MemberSignedUpPayload>> MEMBER_SIGNED_UP_ENVELOPE_TYPE =
            new TypeReference<>() {
            };

    private final CreateWalletUseCase createWalletUseCase;
    private final ObjectMapper objectMapper;

    public MemberCreatedEventConsumer(CreateWalletUseCase createWalletUseCase, ObjectMapper objectMapper) {
        this.createWalletUseCase = createWalletUseCase;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
            topics = KafkaTopics.MEMBER_CREATED,
            groupId = KafkaConsumerGroups.PAYMENT_SERVICE,
            containerFactory = "memberCreatedKafkaListenerContainerFactory"
    )
    public void listen(String eventJson) {
        try {
            EventEnvelope<MemberSignedUpPayload> event = objectMapper.readValue(
                    eventJson,
                    MEMBER_SIGNED_UP_ENVELOPE_TYPE
            );
            validateEvent(event);

            MemberSignedUpPayload payload = event.payload();
            createWalletUseCase.createWallet(new CreateWalletCommand(
                    payload.memberId(),
                    toKoreaLocalDateTime(event.occurredAt())
            ));
        } catch (Exception e) {
            log.error("Failed to process member signed up event envelope.", e);
            throw new RuntimeException("Failed to process member signed up event envelope.", e);
        }
    }

    private void validateEvent(EventEnvelope<MemberSignedUpPayload> event) {
        if (event == null) {
            throw new InvalidChargeRequestException("memberSignedUp event is required.");
        }
        if (event.eventId() == null) {
            throw new InvalidChargeRequestException("eventId is required.");
        }
        if (!MEMBER_SIGNED_UP_EVENT_TYPE.equals(event.eventType())) {
            throw new InvalidChargeRequestException("Unsupported eventType: " + event.eventType());
        }
        if (event.source() == null || event.source().isBlank()) {
            throw new InvalidChargeRequestException("source is required.");
        }
        if (event.recipientId() == null) {
            throw new InvalidChargeRequestException("recipientId is required.");
        }
        if (event.payload() == null) {
            throw new InvalidChargeRequestException("payload is required.");
        }
        if (event.payload().memberId() == null) {
            throw new InvalidChargeRequestException("payload.memberId is required.");
        }
        if (event.payload().email() == null || event.payload().email().isBlank()) {
            throw new InvalidChargeRequestException("payload.email is required.");
        }
        if (event.occurredAt() == null) {
            throw new InvalidChargeRequestException("occurredAt is required.");
        }
        if (!Objects.equals(event.recipientId(), event.payload().memberId())) {
            throw new InvalidChargeRequestException("recipientId and payload.memberId must match.");
        }
    }

    private LocalDateTime toKoreaLocalDateTime(java.time.Instant instant) {
        return LocalDateTime.ofInstant(instant, KOREA_ZONE_ID);
    }
}
