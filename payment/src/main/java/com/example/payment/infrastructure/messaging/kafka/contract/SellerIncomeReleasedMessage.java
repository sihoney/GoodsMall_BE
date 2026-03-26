package com.example.payment.infrastructure.messaging.kafka.contract;

import com.example.payment.domain.enumtype.ConfirmationType;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 판매자 정산 완료를 외부 모듈에 알리는 Kafka 계약 메시지다.
 */
public record SellerIncomeReleasedMessage(
        UUID orderId,
        UUID sellerMemberId,
        UUID sellerWalletId,
        Long releasedAmount,
        LocalDateTime releasedAt,
        ConfirmationType confirmationType
) {
}
