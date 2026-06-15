package com.example.payment.wallet.presentation.dto.response;

import com.example.payment.wallet.application.dto.ChargeCreateResult;
import com.example.payment.wallet.domain.enumtype.ChargeStatus;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * 異⑹쟾 ?붿껌 ?앹꽦 API???묐떟 DTO??
 */
public record ChargeCreateResponse(
        UUID chargeId,
        UUID walletId,
        String pgOrderId,
        BigDecimal amount,
        ChargeStatus chargeStatus
) {

    /**
     * application 寃곌낵瑜?presentation ?묐떟 ?뺤떇?쇰줈 蹂?섑븳??
     */
    public static ChargeCreateResponse from(ChargeCreateResult result) {
        return new ChargeCreateResponse(
                result.chargeId(),
                result.walletId(),
                result.pgOrderId(),
                result.amount(),
                result.chargeStatus()
        );
    }
}
