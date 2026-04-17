package com.example.notification.infrastructure.sse;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Component
public class NotificationSseEmitterRegistry {

    private final ConcurrentHashMap<UUID, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter register(UUID memberId, SseEmitter emitter) {
        SseEmitter previous = emitters.put(memberId, emitter);
        if (previous != null) {
            previous.complete();
        }
        return emitter;
    }

    public Optional<SseEmitter> find(UUID memberId) {
        return Optional.ofNullable(emitters.get(memberId));
    }

    public void remove(UUID memberId) {
        emitters.remove(memberId);
    }
}
