package com.example.payment.application.dto;

import java.util.UUID;

/**
 * seller별 escrow 생성에 필요한 금액 정보를 담는 내부 command다.
 */
public record OrderPaymentSellerCommand(
        UUID sellerMemberId,
        Long sellerReceivableAmount
) {
}
