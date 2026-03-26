package com.example.payment.infrastructure.event;

import com.example.payment.application.event.SellerIncomeReleasedEvent;
import com.example.payment.domain.service.SellerIncomeReleasedEventPublisher;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class SpringSellerIncomeReleasedEventPublisher implements SellerIncomeReleasedEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    public SpringSellerIncomeReleasedEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void publish(SellerIncomeReleasedEvent event) {
        applicationEventPublisher.publishEvent(event);
    }
}
