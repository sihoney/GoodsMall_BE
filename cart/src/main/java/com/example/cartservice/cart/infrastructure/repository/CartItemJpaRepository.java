package com.example.cartservice.cart.infrastructure.repository;

import com.example.cartservice.cart.domain.entity.CartItem;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemJpaRepository extends JpaRepository<CartItem, UUID> {
    List<CartItem> findByCartId(UUID cartId);
    Optional<CartItem> findByCartIdAndProductId(UUID cartId, UUID productId);
    void deleteAllByCartId(UUID cartId);
}
