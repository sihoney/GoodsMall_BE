package com.example.notification.infrastructure.monitoring;

import com.example.notification.application.monitoring.NotificationMetricsRecorder;
import com.example.notification.domain.enumtype.NotificationType;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MicrometerNotificationMetricsRecorder implements NotificationMetricsRecorder {

    private static final String SERVICE_TAG = "notification";
    private static final String CHANNEL_TAG = "sse";

    private final MeterRegistry meterRegistry;

    @Override
    public void recordEventReceived(NotificationType type) {
        increment("notification_event_received_total", type);
    }

    @Override
    public void recordDuplicateEvent(NotificationType type) {
        increment("notification_event_duplicate_total", type);
    }

    @Override
    public void recordSaved(NotificationType type) {
        increment("notification_saved_total", type);
    }

    @Override
    public void recordSaveFailed(NotificationType type, String reason) {
        increment("notification_save_failed_total", type, reason);
    }

    @Override
    public void recordPushAttempt(NotificationType type) {
        increment("notification_push_attempt_total", type);
    }

    @Override
    public void recordPushSuccess(NotificationType type) {
        increment("notification_push_success_total", type);
    }

    @Override
    public void recordPushFailure(NotificationType type, String reason) {
        increment("notification_push_failure_total", type, reason);
    }

    @Override
    public void recordEmitterMissing(NotificationType type) {
        increment("notification_push_emitter_missing_total", type);
    }

    @Override
    public void recordPushSkippedAlreadyPushed(NotificationType type) {
        increment("notification_push_skipped_already_pushed_total", type);
    }

    @Override
    public void recordMarkRead(boolean noop) {
        Counter.builder(noop ? "notification_mark_read_noop_total" : "notification_mark_read_total")
                .tag("service", SERVICE_TAG)
                .register(meterRegistry)
                .increment();
    }

    private void increment(String metricName, NotificationType type) {
        Counter.builder(metricName)
                .tag("service", SERVICE_TAG)
                .tag("channel", CHANNEL_TAG)
                .tag("type", type.name())
                .register(meterRegistry)
                .increment();
    }

    private void increment(String metricName, NotificationType type, String reason) {
        Counter.builder(metricName)
                .tag("service", SERVICE_TAG)
                .tag("channel", CHANNEL_TAG)
                .tag("type", type.name())
                .tag("reason", reason)
                .register(meterRegistry)
                .increment();
    }
}
