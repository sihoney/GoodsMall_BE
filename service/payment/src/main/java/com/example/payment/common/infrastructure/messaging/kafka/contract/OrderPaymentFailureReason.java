package com.example.payment.common.infrastructure.messaging.kafka.contract;

/**
 * 二쇰Ц 寃곗젣 ?ㅽ뙣 寃곌낵?먯꽌 ?ъ슜?섎뒗 ?쒖? ?ㅽ뙣 ?ъ쑀 肄붾뱶??
 */
public enum OrderPaymentFailureReason {
    DUPLICATE_ORDER_PAYMENT,
    WALLET_NOT_FOUND,
    INSUFFICIENT_BALANCE,
    INVALID_REQUEST,
    INTERNAL_ERROR
}
