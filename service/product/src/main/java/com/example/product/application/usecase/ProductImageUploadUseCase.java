package com.example.product.application.usecase;

import com.example.product.presentation.dto.response.ProductImageResponse;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

public interface ProductImageUploadUseCase {
    /**
     * 상품 이미지 업로드
     *
     * @param productId 상품 ID
     * @param file 업로드할 이미지 파일
     * @param sortOrder 정렬 순서
     * @param isThumbnail 썸네일 여부
     * @return 업로드된 이미지 정보
     */
    ProductImageResponse uploadImage(
            UUID productId,
            MultipartFile file,
            Integer sortOrder,
            Boolean isThumbnail
    );
}
