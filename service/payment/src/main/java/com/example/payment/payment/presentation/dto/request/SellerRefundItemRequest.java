package com.example.payment.payment.presentation.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record SellerRefundItemRequest(
        @NotNull(message = "二쇰Ц ??ぉ ID???꾩닔?낅땲??")
        UUID orderItemId
) {
}
