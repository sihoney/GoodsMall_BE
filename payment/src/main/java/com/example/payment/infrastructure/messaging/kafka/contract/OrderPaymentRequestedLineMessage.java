package com.example.payment.infrastructure.messaging.kafka.contract;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * 주문 결제 요청 이벤트 안의 주문 라인 계약 메시지다.
 * payment는 이 목록을 seller별로 집계해 escrow 생성 입력으로 변환한다.
 */
public record OrderPaymentRequestedLineMessage(
        UUID orderItemId,
        UUID sellerId,
        BigDecimal unitPriceSnapshot,
        Integer quantity,
        BigDecimal lineTotalPrice
) {
}
