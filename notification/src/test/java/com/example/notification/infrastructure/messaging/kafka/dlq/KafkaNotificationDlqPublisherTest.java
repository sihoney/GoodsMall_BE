package com.example.notification.infrastructure.messaging.kafka.dlq;

import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import com.example.notification.infrastructure.messaging.kafka.KafkaTopics;
import tools.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

@ExtendWith(MockitoExtension.class)
class KafkaNotificationDlqPublisherTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    private KafkaNotificationDlqPublisher publisher;

    @BeforeEach
    void setUp() {
        publisher = new KafkaNotificationDlqPublisher(kafkaTemplate, JsonMapper.builder().findAndAddModules().build());
    }

    @Test
    void publish_sendsSerializedDlqMessageToKafkaTopic() {
        publisher.publish(
                "listenNotificationEvent",
                "{\"eventType\":\"MEMBER_SIGNED_UP\"}",
                new InvalidEventPayloadException("payload is required."),
                NotificationConsumerFailureDecision.dlq(NotificationDlqReason.INVALID_EVENT_PAYLOAD)
        );

        verify(kafkaTemplate).send(eq(KafkaTopics.NOTIFICATION_DLQ), contains("\"reason\":\"INVALID_EVENT_PAYLOAD\""));
    }
}
