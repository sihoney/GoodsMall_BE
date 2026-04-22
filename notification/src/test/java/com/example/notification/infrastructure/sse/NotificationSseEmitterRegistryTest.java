package com.example.notification.infrastructure.sse;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

class NotificationSseEmitterRegistryTest {

    @Test
    void remove_withEmitter_onlyRemovesMatchingEmitter() {
        NotificationSseEmitterRegistry registry = new NotificationSseEmitterRegistry();
        UUID memberId = UUID.randomUUID();
        SseEmitter first = new SseEmitter();
        SseEmitter second = new SseEmitter();

        registry.register(memberId, first);
        registry.register(memberId, second);

        registry.remove(memberId, first);

        assertThat(registry.find(memberId)).contains(second);
    }
}
