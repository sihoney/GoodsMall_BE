package com.example.cartservice.cart.infrastructure.kafka.event;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CartItemAddedEvent {

    private UUID memberId;
    private UUID productId;
}
