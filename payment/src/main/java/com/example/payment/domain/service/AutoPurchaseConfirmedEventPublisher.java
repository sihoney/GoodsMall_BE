package com.example.payment.domain.service;

import com.example.payment.application.event.AutoPurchaseConfirmedEvent;

public interface AutoPurchaseConfirmedEventPublisher {

    void publish(AutoPurchaseConfirmedEvent event);
}
