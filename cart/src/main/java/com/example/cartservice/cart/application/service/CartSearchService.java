package com.example.cartservice.cart.application.service;

import com.example.cartservice.cart.application.usecase.CartSearchUseCase;
import com.example.cartservice.cart.domain.entity.Cart;
import com.example.cartservice.cart.domain.repository.CartRepository;
import com.example.cartservice.cart.presentation.dto.response.CartResponse;
import com.example.cartservice.cart.presentation.exception.CartNotFoundException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CartSearchService implements CartSearchUseCase {

    private final CartRepository cartRepository;

    @Override
    public CartResponse getMyCart(UUID memberId) {
        if (memberId == null) {
            throw new IllegalArgumentException("회원 ID는 필수입니다");
        }

        Cart cart = cartRepository.findByMemberId(memberId)
            .orElseThrow(CartNotFoundException::new);

        return CartResponse.from(cart);
    }
}
