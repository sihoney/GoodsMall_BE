package com.example.ai.domain.model;

import java.util.UUID;

public record SimilarProductMatch(
        UUID productId,
        double distance
) {
}

