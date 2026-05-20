package com.example.payment.payment.domain.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ?몃? 寃곗젣 寃뚯씠?몄썾???뱀씤/痍⑥냼瑜?異붿긽?뷀븳 ?꾨찓???쒕퉬???ы듃??
 */
public interface TossPaymentGateway {

    TossPaymentConfirmation confirm(String paymentKey, String orderId, BigDecimal amount);

    TossPaymentCancellation cancel(String paymentKey, String cancelReason, BigDecimal cancelAmount);

    /**
     * ?뱀씤 API ?묐떟?먯꽌 application???ъ슜?섎뒗 理쒖냼 ?꾨뱶留??대뒗 寃곌낵 ??낆씠??
     */
    record TossPaymentConfirmation(
            String paymentKey,
            String orderId,
            BigDecimal approvedAmount,
            LocalDateTime approvedAt,
            String method,
            String transferBankCode,
            String cardCompany
    ) {
    }

    /**
     * 痍⑥냼 API ?묐떟?먯꽌 application???ъ슜?섎뒗 理쒖냼 ?꾨뱶留??대뒗 寃곌낵 ??낆씠??
     */
    record TossPaymentCancellation(
            String paymentKey,
            BigDecimal canceledAmount,
            LocalDateTime canceledAt
    ) {
    }
}
