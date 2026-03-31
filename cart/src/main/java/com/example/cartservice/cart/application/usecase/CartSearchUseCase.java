package com.example.cartservice.cart.application.usecase;

import com.example.cartservice.cart.presentation.dto.response.CartResponse;
import java.util.UUID;

public interface CartSearchUseCase {
    CartResponse findCart(UUID memberId);
}
