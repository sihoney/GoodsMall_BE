package com.example.cartservice.cart.application.service;

import com.example.cartservice.cart.application.usecase.CartDeleteUseCase;
import com.example.cartservice.cart.domain.entity.Cart;
import com.example.cartservice.cart.domain.repository.CartRepository;
import com.example.cartservice.cart.presentation.dto.request.DeleteCartItemsRequest;
import com.example.cartservice.cart.presentation.dto.response.CartItemResponse;
import com.example.cartservice.cart.presentation.dto.response.CartResponse;
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
    public CartResponse deleteCartItems(UUID memberId, DeleteCartItemsRequest request) {
        cartRepository.deleteAllByMemberIdAndCartIdIn(memberId, request.getCartItemIds());

        return getCartResponse(memberId);
    }

    @Override
    public void clearCart(UUID memberId) {
        cartRepository.deleteAllByMemberId(memberId);
    }

    @Override
    public void deleteOrderedItems(UUID memberId, List<UUID> productIds) {
        cartRepository.deleteByMemberIdAndProductIdIn(memberId, productIds);
    }

    private CartResponse getCartResponse(UUID memberId) {
        List<Cart> cartItems = cartRepository.findAllByMemberId(memberId);
        List<CartItemResponse> itemResponses = cartItems.stream()
            .map(CartItemResponse::from)
            .collect(Collectors.toList());
        return CartResponse.of(memberId, itemResponses);
    }
}
