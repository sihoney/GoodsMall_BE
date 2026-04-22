package com.example.notification.presentation.controller;

import com.example.notification.infrastructure.sse.NotificationSseEmitterRegistry;
import com.todaylunch.common.security.auth.annotation.CurrentMember;
import com.todaylunch.common.security.auth.dto.AuthenticatedMember;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
@Tag(name = "알림 SSE", description = "알림 SSE API")
public class NotificationSseController {

    private static final Long SSE_TIMEOUT_MS = 30L * 60L * 1000L;
    private static final String CONNECTED_EVENT_NAME = "connected";

    private final NotificationSseEmitterRegistry emitterRegistry;

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "알림 SSE 구독")
    public SseEmitter subscribe(@CurrentMember AuthenticatedMember authenticatedMember) {
        UUID memberId = authenticatedMember.memberId();
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);
        emitterRegistry.register(memberId, emitter);

        emitter.onCompletion(() -> emitterRegistry.remove(memberId, emitter));
        emitter.onTimeout(() -> emitterRegistry.remove(memberId, emitter));
        emitter.onError((throwable) -> emitterRegistry.remove(memberId, emitter));

        try {
            emitter.send(SseEmitter.event()
                    .name(CONNECTED_EVENT_NAME)
                    .data(Map.of("memberId", memberId.toString())));
        } catch (IOException e) {
            emitterRegistry.remove(memberId, emitter);
            emitter.completeWithError(e);
        }

        return emitter;
    }
}
