package com.example.cartservice.cart.presentation.dto.response;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CartResponse {

    private UUID memberId;
    private List<CartItemResponse> items;
    private Integer itemCount;

    public static CartResponse of(UUID memberId, List<CartItemResponse> items) {
        return new CartResponse(
            memberId,
            items,
            items.size()
        );
    }
}
