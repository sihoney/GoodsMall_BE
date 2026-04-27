package com.example.order.application.usecase;

import java.util.UUID;

public interface OrderConfirmUseCase {

    void confirm(UUID orderId, UUID memberId);

    void confirmItem(UUID orderId, UUID orderItemId, UUID memberId);
}
