package com.example.cartservice.cart.presentation.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DeleteOrderedItemsRequest {

    @NotNull(message = "회원 ID는 필수입니다")
    private UUID memberId;

    @NotEmpty(message = "상품 ID 목록은 필수입니다")
    private List<UUID> productIds;
}
