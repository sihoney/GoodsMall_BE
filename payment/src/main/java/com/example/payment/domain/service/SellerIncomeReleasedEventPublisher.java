package com.example.payment.domain.service;

import com.example.payment.application.event.SellerIncomeReleasedEvent;

public interface SellerIncomeReleasedEventPublisher {

    void publish(SellerIncomeReleasedEvent event);
}
