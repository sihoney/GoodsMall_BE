package com.example.notification.infrastructure.messaging.kafka.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import tools.jackson.databind.JsonNode;
import com.todaylunch.common.event.contract.EventEnvelope;
import java.util.List;
import org.junit.jupiter.api.Test;

class NotificationEventHandlerRegistryTest {

    @Test
    void get_returnsRegisteredHandler() {
        NotificationEventHandler handler = new FakeHandler("MEMBER_SIGNED_UP");
        NotificationEventHandlerRegistry registry = new NotificationEventHandlerRegistry(List.of(handler));

        assertThat(registry.get("MEMBER_SIGNED_UP")).isSameAs(handler);
    }

    @Test
    void constructor_rejectsDuplicateEventTypeRegistration() {
        assertThatThrownBy(() -> new NotificationEventHandlerRegistry(List.of(
                new FakeHandler("MEMBER_SIGNED_UP"),
                new FakeHandler("MEMBER_SIGNED_UP")
        )))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("중복된 알림 이벤트 핸들러 등록입니다. eventType=MEMBER_SIGNED_UP");
    }

    @Test
    void get_throwsWhenEventTypeIsUnsupported() {
        NotificationEventHandlerRegistry registry = new NotificationEventHandlerRegistry(List.of(
                new FakeHandler("MEMBER_SIGNED_UP")
        ));

        assertThatThrownBy(() -> registry.get("UNKNOWN"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("지원하지 않는 eventType입니다: UNKNOWN");
    }

    private record FakeHandler(String eventType) implements NotificationEventHandler {

        @Override
        public String supportsEventType() {
            return eventType;
        }

        @Override
        public void handle(EventEnvelope<JsonNode> event) {
        }
    }
}
