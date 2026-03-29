package com.example.cartservice.cart.application.service;

import com.example.cartservice.cart.application.usecase.CartUpdateUseCase;
import com.example.cartservice.cart.domain.entity.Cart;
import com.example.cartservice.cart.domain.entity.CartItem;
import com.example.cartservice.cart.domain.repository.CartItemRepository;
import com.example.cartservice.cart.domain.repository.CartRepository;
import com.example.cartservice.cart.presentation.dto.request.AddCartItemRequest;
import com.example.cartservice.cart.presentation.dto.request.UpdateCartItemRequest;
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
public class CartUpdateService implements CartUpdateUseCase {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    @Override
    public CartResponse addCartItem(UUID memberId, AddCartItemRequest request) {
        if (memberId == null) {
            throw new IllegalArgumentException("회원 ID는 필수입니다");
        }
        if (request == null || request.getProductId() == null || request.getQuantity() == null) {
            throw new IllegalArgumentException("상품 정보는 필수입니다");
        }

        Cart cart = cartRepository.findByMemberId(memberId)
            .orElseGet(() -> cartRepository.save(Cart.create(memberId)));

        CartItem existingItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), request.getProductId())
            .orElse(null);

        if (existingItem != null) {
            existingItem.updateQuantity(existingItem.getQuantity() + request.getQuantity());
            cartItemRepository.save(existingItem);
            return CartResponse.from(cart);
        }

        CartItem newItem = CartItem.create(cart, request.getProductId(), request.getQuantity());
        cart.addItem(newItem);
        cartItemRepository.save(newItem);
        cartRepository.save(cart);

        return CartResponse.from(cart);
    }

    @Override
    public CartResponse updateCartItem(UUID memberId, UUID cartItemId, UpdateCartItemRequest request) {
        if (memberId == null) {
            throw new IllegalArgumentException("회원 ID는 필수입니다");
        }
        if (cartItemId == null) {
            throw new IllegalArgumentException("장바구니 항목 ID는 필수입니다");
        }
        if (request == null || request.getQuantity() == null) {
            throw new IllegalArgumentException("수량은 필수입니다");
        }

        CartItem cartItem = cartItemRepository.findById(cartItemId)
            .orElseThrow(CartItemNotFoundException::new);

        Cart cart = cartItem.getCart();
        validateCartOwnership(cart, memberId);

        cartItem.updateQuantity(request.getQuantity());
        cartItemRepository.save(cartItem);

        return CartResponse.from(cart);
    }

    private void validateCartOwnership(Cart cart, UUID memberId) {
        if (!cart.getMemberId().equals(memberId)) {
            throw new MemberNotAuthorizedException();
        }
    }
}
