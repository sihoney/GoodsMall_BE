package com.example.payment.payment.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * seller蹂?escrow ?앹꽦???꾩슂??湲곕낯 ?뺣낫瑜??대뒗 ?대? command??
 */
public record OrderPaymentSellerCommand(
        UUID sellerMemberId,
        BigDecimal sellerReceivableAmount
) {
}
