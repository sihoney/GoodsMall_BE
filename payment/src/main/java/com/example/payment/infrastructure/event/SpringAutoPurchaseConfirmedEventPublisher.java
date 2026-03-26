package com.example.payment.infrastructure.event;

import com.example.payment.application.event.AutoPurchaseConfirmedEvent;
import com.example.payment.domain.service.AutoPurchaseConfirmedEventPublisher;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class SpringAutoPurchaseConfirmedEventPublisher implements AutoPurchaseConfirmedEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    public SpringAutoPurchaseConfirmedEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void publish(AutoPurchaseConfirmedEvent event) {
        applicationEventPublisher.publishEvent(event);
    }
}
