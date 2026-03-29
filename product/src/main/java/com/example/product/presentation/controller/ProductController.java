package com.example.product.presentation.controller;

import com.example.product.application.usecase.ProductCheckUseCase;
import com.example.product.application.usecase.ProductCreateUseCase;
import com.example.product.application.usecase.ProductDeleteUseCase;
import com.example.product.application.usecase.ProductSearchUseCase;
import com.example.product.application.usecase.ProductUpdateUseCase;
import com.example.product.presentation.dto.request.ProductCheckRequest;
import com.example.product.presentation.dto.request.ProductCreateRequest;
import com.example.product.presentation.dto.request.ProductUpdateRequest;
import com.example.product.presentation.dto.response.ProductAvailabilityResponse;
import com.example.product.presentation.dto.response.ProductResponse;
import java.util.List;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductCreateUseCase productCreateUseCase;
    private final ProductSearchUseCase productSearchUseCase;
    private final ProductUpdateUseCase productUpdateUseCase;
    private final ProductDeleteUseCase productDeleteUseCase;
    private final ProductCheckUseCase productCheckUseCase;

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
     * 상품 구매 가능 여부 조회 API (Order 모듈에서 사용)
     * 상품의 재고와 상태를 확인하여 구매 가능 여부 반환
     *
     * @param productRequests 검사할 상품 목록 (productId, quantity)
     * @return 상품 구매 가능 상태 목록 (200 OK)
     */
    @PostMapping("/check-availability")
    public ResponseEntity<List<ProductAvailabilityResponse>> checkAvailability(
        @Valid @RequestBody List<ProductCheckRequest> productRequests
    ) {
        List<ProductAvailabilityResponse> response = productCheckUseCase.checkAvailability(productRequests);
        return ResponseEntity.ok(response);
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

    @GetMapping("/seller")
    public ResponseEntity<Page<ProductResponse>> findSellerProducts(
        @RequestHeader(value = "X-User-Id", required = false) String sellerId,
                                                              Pageable pageable) {
        Page<ProductResponse> response = productSearchUseCase.findBySellerId(sellerId, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponse> findProduct(@PathVariable String productId) {
        return ResponseEntity.ok(productSearchUseCase.findById(productId));
    }

    /**
     * 상품 수정 API
     *
     * @param userId    판매자 ID (Header: X-User-Id)
     * @param productId 수정할 상품 ID
     * @param request   수정 요청 데이터
     * @return 수정된 상품 정보 (200 OK)
     */
    @PutMapping("/{productId}")
    public ResponseEntity<ProductResponse> updateProduct(
        @RequestHeader(value = "X-User-Id", required = false) String userId,
        @PathVariable String productId,
        @Valid @RequestBody ProductUpdateRequest request
    ) {
        ProductResponse response = productUpdateUseCase.updateProduct(userId, productId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 상품 삭제 API (소프트 삭제)
     *
     * @param userId    판매자 ID (Header: X-User-Id)
     * @param productId 삭제할 상품 ID
     * @return 204 No Content
     */
    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProduct(
        @RequestHeader(value = "X-User-Id", required = false) String userId,
        @PathVariable String productId
    ) {
        productDeleteUseCase.deleteProduct(userId, productId);
        return ResponseEntity.noContent().build();
    }
}
