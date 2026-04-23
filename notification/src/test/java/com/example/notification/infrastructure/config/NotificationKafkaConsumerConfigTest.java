package com.example.notification.infrastructure.config;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import com.example.notification.infrastructure.messaging.kafka.KafkaTopics;
import com.example.notification.infrastructure.messaging.kafka.dlq.model.NotificationConsumerFailureAction;
import com.example.notification.infrastructure.messaging.kafka.dlq.model.NotificationDlqReason;
import com.example.notification.infrastructure.messaging.kafka.dlq.publisher.NotificationDlqPublisher;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.listener.ConsumerRecordRecoverer;

class NotificationKafkaConsumerConfigTest {

    private final NotificationKafkaConsumerConfig config = new NotificationKafkaConsumerConfig();

    @Test
    void notificationDlqRecoverer_publishesTemporaryProcessingErrorToDlq() {
        NotificationDlqPublisher notificationDlqPublisher = org.mockito.Mockito.mock(NotificationDlqPublisher.class);
        ConsumerRecordRecoverer recoverer = config.notificationDlqRecoverer(notificationDlqPublisher);
        ConsumerRecord<String, String> record =
                new ConsumerRecord<>(KafkaTopics.ORDER_CREATED, 0, 0L, "key", "{\"eventType\":\"ORDER_CREATED\"}");

        recoverer.accept(record, new IllegalStateException("temporary failure"));

        verify(notificationDlqPublisher).publish(
                eq("listenNotificationEvent"),
                eq("{\"eventType\":\"ORDER_CREATED\"}"),
                any(RuntimeException.class),
                argThat(decision ->
                        decision.action() == NotificationConsumerFailureAction.DLQ
                                && decision.reason() == NotificationDlqReason.TEMPORARY_PROCESSING_ERROR)
        );
    }
}
