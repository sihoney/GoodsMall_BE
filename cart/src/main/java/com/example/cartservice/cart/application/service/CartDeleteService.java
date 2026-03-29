package com.example.cartservice.cart.application.service;

import com.example.cartservice.cart.application.usecase.CartDeleteUseCase;
import com.example.cartservice.cart.domain.entity.Cart;
import com.example.cartservice.cart.domain.entity.CartItem;
import com.example.cartservice.cart.domain.repository.CartItemRepository;
import com.example.cartservice.cart.domain.repository.CartRepository;
import com.example.cartservice.cart.presentation.dto.request.DeleteCartItemsRequest;
import com.example.cartservice.cart.presentation.dto.response.CartResponse;
import com.example.cartservice.cart.presentation.exception.CartItemNotFoundException;
import com.example.cartservice.cart.presentation.exception.CartNotFoundException;
import com.example.cartservice.cart.presentation.exception.MemberNotAuthorizedException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CartDeleteService implements CartDeleteUseCase {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    @Override
    public CartResponse deleteCartItem(UUID memberId, UUID cartItemId) {
        if (memberId == null) {
            throw new IllegalArgumentException("회원 ID는 필수입니다");
        }
        if (cartItemId == null) {
            throw new IllegalArgumentException("장바구니 항목 ID는 필수입니다");
        }

        CartItem cartItem = cartItemRepository.findById(cartItemId)
            .orElseThrow(CartItemNotFoundException::new);

        Cart cart = cartItem.getCart();
        validateCartOwnership(cart, memberId);

        cart.removeItem(cartItem);
        cartItemRepository.delete(cartItem);
        cartRepository.save(cart);

        return CartResponse.from(cart);
    }

    @Override
    public CartResponse deleteCartItems(UUID memberId, DeleteCartItemsRequest request) {
        if (memberId == null) {
            throw new IllegalArgumentException("회원 ID는 필수입니다");
        }
        if (request == null || request.getCartItemIds() == null || request.getCartItemIds().isEmpty()) {
            throw new IllegalArgumentException("삭제할 장바구니 항목 ID 목록은 필수입니다");
        }

        Cart cart = cartRepository.findByMemberId(memberId)
            .orElseThrow(CartNotFoundException::new);

        validateCartOwnership(cart, memberId);

        for (UUID cartItemId : request.getCartItemIds()) {
            CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(CartItemNotFoundException::new);
            cart.removeItem(cartItem);
            cartItemRepository.delete(cartItem);
        }

        cartRepository.save(cart);
        return CartResponse.from(cart);
    }

    private void validateCartOwnership(Cart cart, UUID memberId) {
        if (!cart.getMemberId().equals(memberId)) {
            throw new MemberNotAuthorizedException();
        }
    }
}
