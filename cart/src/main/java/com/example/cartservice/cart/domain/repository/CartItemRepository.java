package com.example.cartservice.cart.domain.repository;

import com.example.cartservice.cart.domain.entity.CartItem;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CartItemRepository {

    CartItem save(CartItem cartItem);

    Optional<CartItem> findById(UUID cartItemId);

    List<CartItem> findByCartId(UUID cartId);

    Optional<CartItem> findByCartIdAndProductId(UUID cartId, UUID productId);

    void delete(CartItem cartItem);

    void deleteAllByCartId(UUID cartId);

    boolean existsById(UUID cartItemId);
}
