package com.example.cartservice.cart.application.usecase;

import com.example.cartservice.cart.presentation.dto.request.DeleteCartItemsRequest;
import com.example.cartservice.cart.presentation.dto.response.CartResponse;
import java.util.List;
import java.util.UUID;

public interface CartDeleteUseCase {
    CartResponse deleteCartItems(UUID memberId, DeleteCartItemsRequest request);
    void clearCart(UUID memberId);
    void deleteOrderedItems(UUID memberId, List<UUID> productIds);
}
