package com.example.payment.domain.service;

import com.example.payment.application.event.SellerIncomeReleasedEvent;

/**
 * 판매자 정산 완료 내부 이벤트 발행 포트를 정의한다.
 */
public interface SellerIncomeReleasedEventPublisher {

    void publish(SellerIncomeReleasedEvent event);
}
