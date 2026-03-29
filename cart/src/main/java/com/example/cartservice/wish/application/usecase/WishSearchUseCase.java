package com.example.cartservice.wish.application.usecase;

import com.example.cartservice.wish.presentation.dto.response.WishListResponse;
import java.util.UUID;

public interface WishSearchUseCase {
    WishListResponse getMyWishes(UUID memberId);
}
