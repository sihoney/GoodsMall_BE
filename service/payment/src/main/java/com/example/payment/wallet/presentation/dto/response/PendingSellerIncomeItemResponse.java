package com.example.payment.wallet.presentation.dto.response;

import com.example.payment.wallet.application.dto.PendingSellerIncomeItemResult;
import com.example.payment.escrow.domain.enumtype.EscrowStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ?먮ℓ???뺤궛 ?湲?escrow ?④굔 ?묐떟 DTO??
 */
public record PendingSellerIncomeItemResponse(
        UUID escrowId,
        UUID orderId,
        BigDecimal amount,
        EscrowStatus escrowStatus,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    /**
     * application 寃곌낵瑜?presentation ?묐떟?쇰줈 蹂?섑븳??
     */
    public static PendingSellerIncomeItemResponse from(PendingSellerIncomeItemResult result) {
        return new PendingSellerIncomeItemResponse(
                result.escrowId(),
                result.orderId(),
                result.amount(),
                result.escrowStatus(),
                result.createdAt(),
                result.updatedAt()
        );
    }
}
