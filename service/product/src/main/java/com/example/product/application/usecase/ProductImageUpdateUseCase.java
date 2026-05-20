package com.example.product.application.usecase;

import java.util.UUID;

public interface ProductImageUpdateUseCase {

    void changeThumbnail(UUID productId, UUID imageId);
}
