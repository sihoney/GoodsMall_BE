package com.example.cartservice.cart.domain.repository;

import com.example.cartservice.cart.domain.entity.Cart;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CartRepository {

    Cart save(Cart cart);

    Optional<Cart> findById(UUID cartItemId);

    List<Cart> findAllByMemberId(UUID memberId);

    Optional<Cart> findByMemberIdAndProductId(UUID memberId, UUID productId);

    void delete(Cart cart);

    void deleteAllByIdIn(List<UUID> cartItemIds);

    boolean existsById(UUID cartItemId);

    boolean existsByMemberIdAndProductId(UUID memberId, UUID productId);
}
