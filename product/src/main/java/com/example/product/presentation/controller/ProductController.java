package com.example.product.presentation.controller;

import com.example.product.application.usecase.ProductCreateUseCase;
import com.example.product.application.usecase.ProductSearchUseCase;
import com.example.product.presentation.dto.request.ProductCreateRequest;
import com.example.product.presentation.dto.response.ProductResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 상품 API Controller
 */
@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductCreateUseCase productCreateUseCase;
    private final ProductSearchUseCase productSearchUseCase;

    /**
     * 상품 등록 API
     * API Gateway에서 검증된 사용자 정보를 Header로 전달받음
     *
     * @param userId   API Gateway에서 전달된 사용자 ID (Header: X-User-Id)
     *                 개발 중에는 Postman에서 직접 Header 추가하여 테스트
     * @param request  상품 등록 요청 (sellerId는 포함하지 않음)
     * @return 생성된 상품 정보 (201 Created)
     */
    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(
        @RequestHeader(value = "X-User-Id", required = false) String userId,
        @Valid @RequestBody ProductCreateRequest request
    ) {
        ProductResponse response = productCreateUseCase.createProduct(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 사용자용 상품 조회 API (ACTIVE 상품만, 페이징)
     * 구매 가능한 상품만 조회
     *
     * @param pageable 페이징 정보 (page, size, sort)
     * @return 활성 상품 목록 (200 OK)
     */
    @GetMapping
    public ResponseEntity<Page<ProductResponse>> findDisplayProducts(Pageable pageable) {
        Page<ProductResponse> response = productSearchUseCase.findDisplayProducts(pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * 관리자용 전체 상품 조회 API (모든 상태, 페이징)
     * @param pageable 페이징 정보 (page, size, sort)
     * @return 전체 상품 목록 (200 OK)
     * TODO: 추후 @PreAuthorize("hasRole('ADMIN')") 추가 필요
     */
    @GetMapping("/admin/all")
    public ResponseEntity<Page<ProductResponse>> findAllProducts(Pageable pageable) {
        Page<ProductResponse> response = productSearchUseCase.getAllProducts(pageable);
        return ResponseEntity.ok(response);
    }
}
