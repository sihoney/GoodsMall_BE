package com.example.cartservice.cart.application.usecase;

import com.example.cartservice.cart.presentation.dto.request.DeleteCartItemsRequest;
import com.example.cartservice.cart.presentation.dto.response.CartResponse;
import java.util.UUID;

public interface CartDeleteUseCase {
    CartResponse deleteCartItem(UUID memberId, UUID cartItemId);
    CartResponse deleteCartItems(UUID memberId, DeleteCartItemsRequest request);
}
