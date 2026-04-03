package com.example.product.application.usecase;

import com.example.product.presentation.dto.request.ProductCreateRequest;
import com.example.product.presentation.dto.response.ProductResponse;
import org.springframework.web.multipart.MultipartFile;

public interface ProductCreateUseCase {

    /**
     * 상품 생성 (이미지 포함)
     *
     * @param sellerId 판매자 ID
     * @param request 상품 생성 요청
     * @param images 업로드할 이미지 파일 배열 (선택사항)
     * @param thumbnailIndex 썸네일로 사용할 이미지 인덱스 (기본값: 0)
     * @return 생성된 상품 정보 (이미지 포함)
     */
    ProductResponse createProduct(
            String sellerId,
            ProductCreateRequest request,
            MultipartFile[] images,
            Integer thumbnailIndex
    );
}
