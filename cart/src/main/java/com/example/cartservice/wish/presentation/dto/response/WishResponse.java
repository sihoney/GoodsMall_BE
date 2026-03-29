package com.example.cartservice.wish.presentation.dto.response;

import com.example.cartservice.wish.domain.entity.Wish;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class WishResponse {

    private UUID wishId;
    private UUID memberId;
    private UUID productId;
    private LocalDateTime createdAt;

    public static WishResponse from(Wish wish) {
        return new WishResponse(
            wish.getId(),
            wish.getMemberId(),
            wish.getProductId(),
            wish.getCreatedAt()
        );
    }
}
