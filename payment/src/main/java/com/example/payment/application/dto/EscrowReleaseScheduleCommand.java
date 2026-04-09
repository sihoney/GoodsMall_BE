package com.example.payment.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

//todo : orderId가 아닌 orderItemId나 다른 것으로 변경 가능한지 검토할 수 있다.
// 부분 배송 완료 등에 대응하기 위하여 orderId가 아닌 escrowId를 예약 처리의 진입점으로 바꾸는 것을 고려할 수 있다.
public record EscrowReleaseScheduleCommand(
        UUID orderId,
        LocalDateTime deliveredAt
) {
}
