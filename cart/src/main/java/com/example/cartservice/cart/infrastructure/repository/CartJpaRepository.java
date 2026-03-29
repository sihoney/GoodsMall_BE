package com.example.cartservice.cart.infrastructure.repository;

import com.example.cartservice.cart.domain.entity.Cart;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartJpaRepository extends JpaRepository<Cart, UUID> {
    Optional<Cart> findByMemberId(UUID memberId);
    boolean existsByMemberId(UUID memberId);
}
