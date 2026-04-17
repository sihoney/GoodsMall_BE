package com.example.notification.application.service;

import com.example.notification.domain.entity.Notification;
import com.example.notification.domain.enumtype.NotificationStatus;
import com.example.notification.infrastructure.repository.NotificationJpaRepository;
import com.example.notification.infrastructure.sse.NotificationSseEmitterRegistry;
import com.example.notification.presentation.dto.NotificationResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;
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

    @Transactional
    public void push(NotificationResponse notificationResponse) {
        if (notificationResponse == null) {
            return;
        }

        emitterRegistry.find(notificationResponse.memberId())
                .ifPresentOrElse(
                        emitter -> sendAndMark(notificationResponse, emitter),
                        () -> log.debug(
                                "No active SSE emitter found. notificationId={} memberId={}",
                                notificationResponse.notificationId(),
                                notificationResponse.memberId()
                        )
                );
    }

    private void sendAndMark(NotificationResponse notificationResponse, SseEmitter emitter) {
        try {
            emitter.send(SseEmitter.event()
                    .name(SSE_EVENT_NAME)
                    .data(notificationResponse));
            markStatus(notificationResponse.notificationId(), NotificationStatus.PUSHED);
        } catch (IOException e) {
            markStatus(notificationResponse.notificationId(), NotificationStatus.FAILED);
            emitterRegistry.remove(notificationResponse.memberId());
            emitter.completeWithError(e);
            log.warn(
                    "Failed to push SSE notification. notificationId={} memberId={}",
                    notificationResponse.notificationId(),
                    notificationResponse.memberId(),
                    e
            );
        }
    }

    private void markStatus(UUID notificationId, NotificationStatus status) {
        Notification notification = notificationJpaRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("notification not found: " + notificationId));
        notification.changeStatus(status, LocalDateTime.now());
    }
}
