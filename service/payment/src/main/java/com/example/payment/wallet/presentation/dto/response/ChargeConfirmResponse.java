package com.example.payment.wallet.presentation.dto.response;

import com.example.payment.wallet.application.dto.ChargeConfirmResult;
import com.example.payment.wallet.domain.enumtype.ChargeStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 異⑹쟾 ?뺤씤 API???묐떟 DTO??
 */
public record ChargeConfirmResponse(
        UUID chargeId,
        ChargeStatus chargeStatus,
        BigDecimal approvedAmount,
        BigDecimal walletBalance,
        LocalDateTime approvedAt
) {

    /**
     * application 寃곌낵瑜?presentation ?묐떟 ?뺤떇?쇰줈 蹂?섑븳??
     */
    public static ChargeConfirmResponse from(ChargeConfirmResult result) {
        return new ChargeConfirmResponse(
                result.chargeId(),
                result.chargeStatus(),
                result.approvedAmount(),
                result.walletBalance(),
                result.approvedAt()
        );
    }
}
