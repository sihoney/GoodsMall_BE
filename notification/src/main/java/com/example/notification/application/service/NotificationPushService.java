package com.example.notification.application.service;

import com.example.notification.application.monitoring.NotificationMetricsRecorder;
import com.example.notification.domain.entity.Notification;
import com.example.notification.domain.enumtype.NotificationMetricReason;
import com.example.notification.domain.enumtype.NotificationStatus;
import com.example.notification.infrastructure.repository.NotificationJpaRepository;
import com.example.notification.infrastructure.sse.NotificationSseEmitterRegistry;
import com.example.notification.presentation.dto.NotificationResponse;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationPushService {

    private static final String SSE_EVENT_NAME = "notification";

    private final NotificationJpaRepository notificationJpaRepository;
    private final NotificationSseEmitterRegistry emitterRegistry;
    private final NotificationMetricsRecorder notificationMetricsRecorder;

    @Transactional
    public void push(NotificationResponse notificationResponse) {
        if (notificationResponse == null) {
            return;
        }

        notificationMetricsRecorder.recordPushAttempt(notificationResponse.type()); // 푸시 시도 계측

        Notification notification = notificationJpaRepository.findById(notificationResponse.notificationId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "notification not found: " + notificationResponse.notificationId()
                ));

        if (notification.hasStatus(NotificationStatus.PUSHED)) {
            notificationMetricsRecorder.recordPushSkippedAlreadyPushed(notificationResponse.type()); // 이미 pushed no-op 계측
            log.debug(
                    "Push skipped because notification is already pushed. notificationId={} memberId={}",
                    notificationResponse.notificationId(),
                    notificationResponse.memberId()
            );
            return;
        }

        emitterRegistry.find(notificationResponse.memberId())
                .ifPresentOrElse(
                        emitter -> sendAndMark(notificationResponse, emitter, notification),
                        () -> {
                            notificationMetricsRecorder.recordEmitterMissing(notificationResponse.type()); // 푸시 실패 계측 - emitter 누락
                            log.debug(
                                    "No active SSE emitter found. notificationId={} memberId={}",
                                    notificationResponse.notificationId(),
                                    notificationResponse.memberId()
                            );
                        }
                );
    }

    private void sendAndMark(NotificationResponse notificationResponse, SseEmitter emitter, Notification notification) {
        try {
            emitter.send(SseEmitter.event()
                    .name(SSE_EVENT_NAME)
                    .data(notificationResponse));
            markStatus(notification, NotificationStatus.PUSHED);
            notificationMetricsRecorder.recordPushSuccess(notificationResponse.type()); // 푸시 성공 계측
        } catch (Exception e) {
            markStatus(notification, NotificationStatus.FAILED);
            notificationMetricsRecorder.recordPushFailure(
                    notificationResponse.type(),
                    NotificationMetricReason.SEND_EXCEPTION.name()
            ); // 푸시 실패 계측 - 전송 예외
            emitterRegistry.remove(notificationResponse.memberId(), emitter);
            emitter.completeWithError(e);

            log.warn(
                    "Failed to push SSE notification. notificationId={} memberId={}",
                    notificationResponse.notificationId(),
                    notificationResponse.memberId(),
                    e
            );
        }
    }

    private void markStatus(Notification notification, NotificationStatus status) {
        if (notification.hasStatus(status)) {
            return;
        }

        notification.changeStatus(status, LocalDateTime.now());
        notificationJpaRepository.saveAndFlush(notification);
    }
}
