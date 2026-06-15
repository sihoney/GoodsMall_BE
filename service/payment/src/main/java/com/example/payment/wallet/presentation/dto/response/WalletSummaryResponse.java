package com.example.payment.wallet.presentation.dto.response;

import com.example.payment.wallet.application.dto.WalletSummaryResult;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * wallet ?붿빟 議고쉶 ?묐떟 DTO??
 */
public record WalletSummaryResponse(
        UUID walletId,
        UUID memberId,
        BigDecimal balance,
        LocalDateTime updatedAt
) {

    /**
     * application 寃곌낵瑜?presentation ?묐떟?쇰줈 蹂?섑븳??
     */
    public static WalletSummaryResponse from(WalletSummaryResult result) {
        return new WalletSummaryResponse(
                result.walletId(),
                result.memberId(),
                result.balance(),
                result.updatedAt()
        );
    }
}
