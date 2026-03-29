package com.example.cartservice.cart.presentation.dto.response;

import com.example.cartservice.cart.domain.entity.CartItem;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponse {

    private UUID cartItemId;
    private UUID productId;
    private Integer quantity;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CartItemResponse from(CartItem cartItem) {
        return new CartItemResponse(
            cartItem.getId(),
            cartItem.getProductId(),
            cartItem.getQuantity(),
            cartItem.getCreatedAt(),
            cartItem.getUpdatedAt()
        );
    }
}
