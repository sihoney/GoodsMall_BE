package com.example.payment.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * wallet 단건 요약 조회 결과다.
 */
public record WalletSummaryResult(
        UUID walletId,
        UUID memberId,
        Long balance,
        LocalDateTime updatedAt
) {
}
