package com.example.notification.infrastructure.messaging.kafka.dlq;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class DefaultNotificationConsumerExceptionClassifierTest {

    private final DefaultNotificationConsumerExceptionClassifier classifier =
            new DefaultNotificationConsumerExceptionClassifier();

    @Test
    void classify_returnsDlqForIllegalArgumentException() {
        NotificationConsumerFailureDecision decision =
                classifier.classify(new IllegalArgumentException("invalid payload"));

        assertThat(decision.action()).isEqualTo(NotificationConsumerFailureAction.DLQ);
        assertThat(decision.reason()).isEqualTo(NotificationDlqReason.INVALID_EVENT_PAYLOAD);
    }

    @Test
    void classify_returnsParseFailureForEventParseException() {
        NotificationConsumerFailureDecision decision =
                classifier.classify(new EventParseException("parse failed", new RuntimeException("boom")));

        assertThat(decision.action()).isEqualTo(NotificationConsumerFailureAction.DLQ);
        assertThat(decision.reason()).isEqualTo(NotificationDlqReason.EVENT_PARSE_FAILURE);
    }

    @Test
    void classify_returnsUnsupportedEventTypeReason() {
        NotificationConsumerFailureDecision decision =
                classifier.classify(new UnsupportedEventTypeException("UNKNOWN"));

        assertThat(decision.action()).isEqualTo(NotificationConsumerFailureAction.DLQ);
        assertThat(decision.reason()).isEqualTo(NotificationDlqReason.UNSUPPORTED_EVENT_TYPE);
    }

    @Test
    void classify_returnsInvalidPayloadReasonForTypedPayloadException() {
        NotificationConsumerFailureDecision decision =
                classifier.classify(new InvalidEventPayloadException("payload missing"));

        assertThat(decision.action()).isEqualTo(NotificationConsumerFailureAction.DLQ);
        assertThat(decision.reason()).isEqualTo(NotificationDlqReason.INVALID_EVENT_PAYLOAD);
    }

    @Test
    void classify_returnsRetryForUnexpectedRuntimeException() {
        NotificationConsumerFailureDecision decision =
                classifier.classify(new IllegalStateException("temporary db issue"));

        assertThat(decision.action()).isEqualTo(NotificationConsumerFailureAction.RETRY);
        assertThat(decision.reason()).isEqualTo(NotificationDlqReason.TEMPORARY_PROCESSING_ERROR);
    }
}
