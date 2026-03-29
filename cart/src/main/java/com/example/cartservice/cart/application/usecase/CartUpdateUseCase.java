package com.example.cartservice.cart.application.usecase;

import com.example.cartservice.cart.presentation.dto.request.AddCartItemRequest;
import com.example.cartservice.cart.presentation.dto.request.UpdateCartItemRequest;
import com.example.cartservice.cart.presentation.dto.response.CartResponse;
import java.util.UUID;

public interface CartUpdateUseCase {
    CartResponse addCartItem(UUID memberId, AddCartItemRequest request);
    CartResponse updateCartItem(UUID memberId, UUID cartItemId, UpdateCartItemRequest request);
}
