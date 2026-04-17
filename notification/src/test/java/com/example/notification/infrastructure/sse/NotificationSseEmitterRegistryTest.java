package com.example.notification.infrastructure.sse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

class NotificationSseEmitterRegistryTest {

    private NotificationSseEmitterRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new NotificationSseEmitterRegistry();
    }

    @Test
    void registerAndFindEmitter() {
        UUID memberId = UUID.randomUUID();
        SseEmitter emitter = new SseEmitter();

        registry.register(memberId, emitter);

        assertThat(registry.find(memberId)).contains(emitter);
    }

    @Test
    void registerReplacesPreviousEmitter() {
        UUID memberId = UUID.randomUUID();
        SseEmitter previous = mock(SseEmitter.class);
        SseEmitter next = new SseEmitter();

        registry.register(memberId, previous);
        registry.register(memberId, next);

        verify(previous).complete();
        assertThat(registry.find(memberId)).contains(next);
    }

    @Test
    void removeEmitter() {
        UUID memberId = UUID.randomUUID();
        registry.register(memberId, new SseEmitter());

        registry.remove(memberId);

        assertThat(registry.find(memberId)).isEmpty();
    }
}
