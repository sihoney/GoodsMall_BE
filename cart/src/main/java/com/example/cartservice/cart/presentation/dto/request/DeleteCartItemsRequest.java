package com.example.cartservice.cart.presentation.dto.request;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DeleteCartItemsRequest {

    @NotEmpty(message = "장바구니 아이템 ID 목록은 필수입니다")
    private List<UUID> cartItemIds;
}
