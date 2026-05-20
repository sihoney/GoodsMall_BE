package com.example.payment.common.infrastructure.messaging.kafka.contract;

import com.example.payment.common.domain.enumtype.ConfirmationType;
import java.time.Instant;
import java.util.UUID;

/**
 * 二쇰Ц 援щℓ?뺤젙 ?대깽?몄쓽 Kafka 怨꾩빟 硫붿떆吏??
 */
public record OrderPurchaseConfirmedMessage(
        String eventId,
        UUID orderId,
        UUID sellerMemberId,
        Instant confirmedAt,
        ConfirmationType confirmationType // 援щℓ ?뺤젙 諛⑹떇
) {
}
