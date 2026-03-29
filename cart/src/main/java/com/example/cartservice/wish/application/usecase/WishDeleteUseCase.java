package com.example.cartservice.wish.application.usecase;

import java.util.UUID;

public interface WishDeleteUseCase {
    void moveWishToCart(UUID memberId, UUID wishId);
}
