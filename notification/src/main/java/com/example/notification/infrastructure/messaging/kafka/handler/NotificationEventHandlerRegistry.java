package com.example.notification.infrastructure.messaging.kafka.handler;

import com.example.notification.infrastructure.messaging.kafka.dlq.UnsupportedEventTypeException;
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
                throw new IllegalStateException("Duplicate notification event handler registration. eventType=" + eventType);
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
