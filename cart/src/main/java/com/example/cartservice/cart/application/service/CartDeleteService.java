package com.example.cartservice.cart.application.service;

import com.example.cartservice.cart.application.usecase.CartDeleteUseCase;
import com.example.cartservice.cart.domain.entity.Cart;
import com.example.cartservice.cart.domain.repository.CartRepository;
import com.example.cartservice.cart.presentation.dto.request.DeleteCartItemsRequest;
import com.example.cartservice.cart.presentation.dto.response.CartItemResponse;
import com.example.cartservice.cart.presentation.dto.response.CartResponse;
import com.example.cartservice.cart.presentation.exception.CartItemNotFoundException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CartDeleteService implements CartDeleteUseCase {

    private final CartRepository cartRepository;

    @Override
    public CartResponse deleteCartItem(UUID memberId, UUID cartItemId) {
        validateDeleteCartItemParams(memberId, cartItemId);

        Cart cart = findCartItem(cartItemId);
        cart.validateOwner(memberId);

        cartRepository.delete(cart);

        return getCartResponse(memberId);
    }

    @Override
    public CartResponse deleteCartItems(UUID memberId, DeleteCartItemsRequest request) {
        validateDeleteCartItemsParams(memberId, request);

        cartRepository.deleteAllByIdIn(request.getCartItemIds());

        return getCartResponse(memberId);
    }

    private void validateDeleteCartItemParams(UUID memberId, UUID cartItemId) {
        if (memberId == null) {
            throw new IllegalArgumentException("회원 ID는 필수입니다");
        }
        if (cartItemId == null) {
            throw new IllegalArgumentException("장바구니 항목 ID는 필수입니다");
        }
    }

    private void validateDeleteCartItemsParams(UUID memberId, DeleteCartItemsRequest request) {
        if (memberId == null) {
            throw new IllegalArgumentException("회원 ID는 필수입니다");
        }
        if (request == null || request.getCartItemIds() == null || request.getCartItemIds().isEmpty()) {
            throw new IllegalArgumentException("삭제할 장바구니 항목 ID 목록은 필수입니다");
        }
    }

    private Cart findCartItem(UUID cartItemId) {
        return cartRepository.findById(cartItemId)
            .orElseThrow(CartItemNotFoundException::new);
    }

    private CartResponse getCartResponse(UUID memberId) {
        List<Cart> cartItems = cartRepository.findAllByMemberId(memberId);
        List<CartItemResponse> itemResponses = cartItems.stream()
            .map(CartItemResponse::from)
            .collect(Collectors.toList());
        return CartResponse.of(memberId, itemResponses);
    }
}
