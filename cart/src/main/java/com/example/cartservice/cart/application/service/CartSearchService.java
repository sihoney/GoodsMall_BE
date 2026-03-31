package com.example.cartservice.cart.application.service;

import com.example.cartservice.cart.application.usecase.CartSearchUseCase;
import com.example.cartservice.cart.domain.entity.Cart;
import com.example.cartservice.cart.domain.repository.CartRepository;
import com.example.cartservice.cart.presentation.dto.response.CartItemResponse;
import com.example.cartservice.cart.presentation.dto.response.CartResponse;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
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
        validateMemberId(memberId);

        List<Cart> cartItems = cartRepository.findAllByMemberId(memberId);

        List<CartItemResponse> itemResponses = cartItems.stream()
            .map(CartItemResponse::from)
            .collect(Collectors.toList());

        return CartResponse.of(memberId, itemResponses);
    }

    private void validateMemberId(UUID memberId) {
        if (memberId == null) {
            throw new IllegalArgumentException("회원 ID는 필수입니다");
        }
    }
}
