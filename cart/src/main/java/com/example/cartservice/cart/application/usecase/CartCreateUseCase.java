package com.example.cartservice.cart.application.usecase;

import com.example.cartservice.cart.presentation.dto.response.CartResponse;
import java.util.UUID;

public interface CartCreateUseCase {
    CartResponse createCart(UUID memberId);
}
