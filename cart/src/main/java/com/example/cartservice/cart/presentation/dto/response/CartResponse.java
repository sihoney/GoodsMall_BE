package com.example.cartservice.cart.presentation.dto.response;

import com.example.cartservice.cart.domain.entity.Cart;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CartResponse {

    private UUID cartId;
    private UUID memberId;
    private List<CartItemResponse> items;
    private Integer itemCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CartResponse from(Cart cart) {
        return new CartResponse(
            cart.getId(),
            cart.getMemberId(),
            cart.getItems().stream()
                .map(CartItemResponse::from)
                .collect(Collectors.toList()),
            cart.getItemCount(),
            cart.getCreatedAt(),
            cart.getUpdatedAt()
        );
    }
}
