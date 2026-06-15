package com.example.cartservice.cart.presentation.dto.response;

import com.example.cartservice.cart.domain.entity.Cart;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponse {

    private UUID cartId;
    private UUID productId;
    private Integer quantity;
    private LocalDateTime addedAt;

    public static CartItemResponse from(Cart cart) {
        return new CartItemResponse(
            cart.getCartId(),
            cart.getProductId(),
            cart.getQuantity(),
            cart.getAddedAt()
        );
    }
}
