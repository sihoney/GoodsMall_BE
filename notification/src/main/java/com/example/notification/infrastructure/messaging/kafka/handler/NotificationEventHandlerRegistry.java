package com.example.notification.infrastructure.messaging.kafka.handler;

import com.example.notification.infrastructure.messaging.kafka.dlq.exception.UnsupportedEventTypeException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class NotificationEventHandlerRegistry {

    private final Map<String, NotificationEventHandler> handlersByEventType;

    public NotificationEventHandlerRegistry(List<NotificationEventHandler> handlers) {
        this.handlersByEventType = new LinkedHashMap<>();

        for (NotificationEventHandler handler : handlers) {
            String eventType = handler.supportsEventType();
            if (handlersByEventType.containsKey(eventType)) {
                throw new IllegalStateException("중복된 알림 이벤트 핸들러 등록입니다. eventType=" + eventType);
            }
            handlersByEventType.put(eventType, handler);
        }
    }

    public NotificationEventHandler get(String eventType) {
        NotificationEventHandler handler = handlersByEventType.get(eventType);
        if (handler == null) {
            throw new UnsupportedEventTypeException(eventType);
        }
        return handler;
    }
}
