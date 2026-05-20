package com.example.payment.payment.presentation.dto.request;

import com.example.payment.payment.domain.enumtype.PaymentRefundType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record PaymentCancellationRequest(
        @NotNull(message = "二쇰Ц ID???꾩닔?낅땲??")
        UUID orderId,

        @NotNull(message = "援щℓ???뚯썝 ID???꾩닔?낅땲??")
        UUID buyerMemberId,

        @NotNull(message = "二쇰Ц 痍⑥냼 ?붿껌 ID???꾩닔?낅땲??")
        UUID orderCancelRequestId,

        @NotNull(message = "?섎텋 ?좏삎? ?꾩닔?낅땲??")
        PaymentRefundType refundType,

        String reason,

        @NotEmpty(message = "??ぉ? 鍮꾩뼱 ?덉쓣 ???놁뒿?덈떎.")
        List<@Valid PaymentRefundItemRequest> items
) {
}
