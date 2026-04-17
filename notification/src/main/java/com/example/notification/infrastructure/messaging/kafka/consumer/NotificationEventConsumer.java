package com.example.notification.infrastructure.messaging.kafka.consumer;

import com.example.notification.application.dto.NotificationCommand;
import com.example.notification.application.mapper.NotificationEventMapper;
import com.example.notification.application.usecase.NotificationUsecase;
import com.example.notification.infrastructure.messaging.kafka.contract.MemberSignedUpPayload;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.todaylunch.common.event.contract.EventEnvelope;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationEventConsumer {

    private static final TypeReference<EventEnvelope<MemberSignedUpPayload>> MEMBER_SIGNED_UP_ENVELOPE_TYPE =
            new TypeReference<>() {
            };

    private final NotificationEventMapper notificationEventMapper;
    private final NotificationUsecase notificationUsecase;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "${notification.kafka.topics.member-signed-up:member-signed-up}",
            groupId = "${notification.kafka.consumer-groups.member-signed-up:notification-service}",
            containerFactory = "memberSignedUpKafkaListenerContainerFactory"
    )
    public void listenMemberSignedUp(String message) {
        NotificationCommand command = notificationEventMapper.toCommand(parseEnvelope(message));
        notificationUsecase.createNotification(command);
    }

    private EventEnvelope<MemberSignedUpPayload> parseEnvelope(String message) {
        try {
            return objectMapper.readValue(message, MEMBER_SIGNED_UP_ENVELOPE_TYPE);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to parse member signed up event envelope.", e);
        }
    }
}
