package com.example.payment.presentation.dto.request;

import com.example.payment.domain.enumtype.PaymentRefundType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record SellerRefundConfirmRequest(
        @NotNull(message = "주문 ID는 필수입니다.")
        UUID orderId,

        @NotNull(message = "주문 취소 요청 ID는 필수입니다.")
        UUID orderCancelRequestId,

        @NotNull(message = "환불 유형은 필수입니다.")
        PaymentRefundType refundType,

        String reason,

        @NotEmpty(message = "항목은 비어 있을 수 없습니다.")
        List<@Valid SellerRefundItemRequest> items
) {
}
