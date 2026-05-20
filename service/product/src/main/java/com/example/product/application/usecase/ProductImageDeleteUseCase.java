package com.example.product.application.usecase;

import java.util.UUID;

/**
 * 상품 이미지 삭제 UseCase
 */
public interface ProductImageDeleteUseCase {

    /**
     * 상품 이미지 삭제
     * 썸네일 이미지 삭제 시 남은 이미지 중 첫 번째를 썸네일로 자동 지정
     *
     * @param productId 상품 ID
     * @param imageId   이미지 ID
     */
    void deleteImage(UUID productId, UUID imageId);
}
