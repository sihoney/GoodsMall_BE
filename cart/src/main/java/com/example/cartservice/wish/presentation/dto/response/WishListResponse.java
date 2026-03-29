package com.example.cartservice.wish.presentation.dto.response;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class WishListResponse {

    private List<UUID> productIds;
    private int totalCount;
}
