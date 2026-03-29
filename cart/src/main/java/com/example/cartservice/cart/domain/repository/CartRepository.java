package com.example.cartservice.cart.domain.repository;

import com.example.cartservice.cart.domain.entity.Cart;
import java.util.Optional;
import java.util.UUID;

public interface CartRepository {

    Cart save(Cart cart);

    Optional<Cart> findById(UUID cartId);

    Optional<Cart> findByMemberId(UUID memberId);

    void delete(Cart cart);

    boolean existsById(UUID cartId);

    boolean existsByMemberId(UUID memberId);
}
