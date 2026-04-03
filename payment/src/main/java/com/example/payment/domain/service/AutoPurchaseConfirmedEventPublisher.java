package com.example.payment.domain.service;

import com.example.payment.application.event.AutoPurchaseConfirmedEvent;

/**
 * 자동 구매확정 내부 이벤트 발행 포트를 정의한다.
 */
public interface AutoPurchaseConfirmedEventPublisher {

    void publish(AutoPurchaseConfirmedEvent event);
}
