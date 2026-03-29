package com.example.cartservice.cart.application.service;

import com.example.cartservice.cart.application.usecase.CartCreateUseCase;
import com.example.cartservice.cart.domain.entity.Cart;
import com.example.cartservice.cart.domain.repository.CartRepository;
import com.example.cartservice.cart.presentation.dto.response.CartResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CartCreateService implements CartCreateUseCase {

    private final CartRepository cartRepository;

    @Override
    public CartResponse createCart(UUID memberId) {
        if (memberId == null) {
            throw new IllegalArgumentException("회원 ID는 필수입니다");
        }

        Cart cart = Cart.create(memberId);
        Cart savedCart = cartRepository.save(cart);

        return CartResponse.from(savedCart);
    }
}
