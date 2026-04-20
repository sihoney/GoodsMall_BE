package com.example.notification.infrastructure.messaging.kafka.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.todaylunch.common.event.contract.EventEnvelope;

public interface NotificationEventHandler {

    String supportsEventType();

    void handle(EventEnvelope<JsonNode> event);
}
