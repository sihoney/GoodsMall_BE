package com.example.cartservice.wish.application.service;

import com.example.cartservice.cart.application.usecase.CartUpdateUseCase;
import com.example.cartservice.cart.presentation.dto.request.AddCartItemRequest;
import com.example.cartservice.wish.application.usecase.WishDeleteUseCase;
import com.example.cartservice.wish.domain.entity.Wish;
import com.example.cartservice.wish.domain.repository.WishRepository;
import com.example.cartservice.wish.presentation.exception.WishNotFoundException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class WishDeleteService implements WishDeleteUseCase {

    private final WishRepository wishRepository;
    private final CartUpdateUseCase cartUpdateUseCase;

    @Override
    public void moveToCart(UUID memberId, UUID wishId) {
        Wish wish = wishRepository.findById(wishId)
                .orElseThrow(WishNotFoundException::new);

        wish.validateWishOwner(memberId);

        AddCartItemRequest request = new AddCartItemRequest(wish.getProductId(), 1);
        cartUpdateUseCase.addCartItem(memberId, request);

        wishRepository.delete(wish);
    }
}
