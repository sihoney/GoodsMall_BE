package com.example.payment.common.infrastructure.messaging.kafka.contract;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * payment -> settlement ?뺤궛 吏湲?寃곌낵 ?대깽??怨꾩빟(contract)?대떎.
 * <p>
 * 異붿쟻 ??湲곗?:
 * - eventId: ???대깽?몄쓽 怨좎쑀 ?앸퀎?? * - requestEventId: ?먯씤 ?붿껌 ?대깽??SellerSettlementPayoutRequestedMessage)??eventId ???쒕룄 ?대젰 異붿쟻
 * - settlementId: ?뺤궛 硫깅벑 湲곕낯 ????吏湲?1?뚯꽦 蹂댁옣
 */
public record SellerSettlementPayoutResultMessage(
        UUID eventId,
        UUID requestEventId,
        UUID settlementId,
        UUID sellerMemberId,
        BigDecimal payoutAmount,
        SellerSettlementPayoutResultStatus resultStatus,
        PayoutFailureReason failureReason,
        LocalDateTime processedAt
) {
}

