package com.example.payment.presentation.dto.response;

import com.example.payment.application.dto.WalletSummaryResult;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * wallet 요약 조회 응답 DTO다.
 */
public record WalletSummaryResponse(
        UUID walletId,
        UUID memberId,
        Long balance,
        LocalDateTime updatedAt
) {

    /**
     * application 결과를 presentation 응답으로 변환한다.
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
