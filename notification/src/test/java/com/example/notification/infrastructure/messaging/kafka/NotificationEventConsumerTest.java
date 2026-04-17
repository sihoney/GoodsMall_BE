package com.example.notification.infrastructure.messaging.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import com.example.notification.application.dto.NotificationCommand;
import com.example.notification.application.mapper.NotificationEventMapper;
import com.example.notification.application.usecase.NotificationUsecase;
import com.example.notification.domain.enumtype.NotificationReferenceType;
import com.example.notification.domain.enumtype.NotificationType;
import com.example.notification.infrastructure.messaging.kafka.consumer.NotificationEventConsumer;
import com.example.notification.infrastructure.messaging.kafka.contract.MemberSignedUpPayload;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.todaylunch.common.event.contract.EventEnvelope;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationEventConsumerTest {

    @Mock
    private NotificationUsecase notificationUsecase;

    private NotificationEventConsumer consumer;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().findAndRegisterModules();
        consumer = new NotificationEventConsumer(new NotificationEventMapper(), notificationUsecase, objectMapper);
    }

    @Test
    void listenMemberSignedUp_dispatchesNotificationCommand() throws Exception {
        UUID eventId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        Instant occurredAt = Instant.parse("2026-03-29T09:49:58Z");
        EventEnvelope<MemberSignedUpPayload> envelope = new EventEnvelope<>(
                eventId,
                "MEMBER_SIGNED_UP",
                "member-service",
                memberId,
                memberId,
                occurredAt,
                "mock-trace-id",
                new MemberSignedUpPayload(memberId, "user@example.com")
        );
        String message = objectMapper.writeValueAsString(envelope);

        consumer.listenMemberSignedUp(message);

        ArgumentCaptor<NotificationCommand> captor = ArgumentCaptor.forClass(NotificationCommand.class);
        verify(notificationUsecase).createNotification(captor.capture());

        NotificationCommand command = captor.getValue();
        assertThat(command.eventId()).isEqualTo(eventId);
        assertThat(command.traceId()).isEqualTo("mock-trace-id");
        assertThat(command.memberId()).isEqualTo(memberId);
        assertThat(command.type()).isEqualTo(NotificationType.MEMBER_SIGNED_UP);
        assertThat(command.referenceId()).isNull();
        assertThat(command.referenceType()).isNull();
        assertThat(command.title()).isEqualTo("Welcome to TodayLunch");
        assertThat(command.content()).isEqualTo("Your account registration is complete.");
        assertThat(command.occurredAt()).isEqualTo(LocalDateTime.of(2026, 3, 29, 18, 49, 58));
    }
}
