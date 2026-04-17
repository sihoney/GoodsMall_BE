package com.example.product.presentation.controller;

import com.example.product.application.usecase.ProductCreateUseCase;
import com.example.product.application.usecase.ProductDeleteUseCase;
import com.example.product.application.usecase.ProductSearchUseCase;
import com.example.product.application.usecase.ProductUpdateUseCase;
import com.example.product.presentation.dto.request.ProductCheckRequest;
import com.example.product.presentation.dto.request.ProductCreateRequest;
import com.example.product.presentation.dto.request.ProductStatusUpdateRequest;
import com.example.product.presentation.dto.request.ProductUpdateRequest;
import com.example.product.presentation.dto.request.StockAdjustmentRequest;
import com.example.product.presentation.dto.response.ProductAvailabilityResponse;
import com.example.product.presentation.dto.response.ProductResponse;
import com.example.product.presentation.util.MultipartJsonParser;
import com.todaylunch.common.security.auth.annotation.CurrentMember;
import com.todaylunch.common.security.auth.dto.AuthenticatedMember;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;


@Tag(name = "Product", description = "상품 관리 API")
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductCreateUseCase productCreateUseCase;
    private final ProductSearchUseCase productSearchUseCase;
    private final ProductUpdateUseCase productUpdateUseCase;
    private final ProductDeleteUseCase productDeleteUseCase;
    private final MultipartJsonParser jsonParser;

    @Operation(
            summary = "상품 등록",
            description = "상품 정보와 이미지를 함께 등록합니다. 이미지는 선택사항입니다.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "상품 생성 성공",
                            content = @Content(schema = @Schema(implementation = ProductResponse.class))),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청"),
                    @ApiResponse(responseCode = "401", description = "인증 실패")
            }
    )
    @Parameters({
            @Parameter(name = "X-Member-Id", description = "회원 ID (UUID)", required = true,
                    in = ParameterIn.HEADER, example = "550e8400-e29b-41d4-a716-446655440000"),
            @Parameter(name = "X-Member-Role", description = "회원 역할 (USER, SELLER, ADMIN)", required = true,
                    in = ParameterIn.HEADER, example = "SELLER")
    })
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<ProductResponse> createProduct(
            @Parameter(hidden = true) @CurrentMember AuthenticatedMember authenticatedMember,
            @Parameter(description = "상품 정보 (JSON 문자열)", required = true,
                    example = "{\"title\":\"햄버거포카\",\"description\":\"햄버거 포토 카드\",\"price\":1000,\"stockQuantity\":90000,\"categoryId\":\"c325102d-2853-42b8-a5f4-23cd7b9adcac\"}")
            @RequestPart("productData") String productDataJson,
            @Parameter(description = "상품 이미지 파일 배열 (최대 10개)", required = false)
            @RequestPart(value = "images", required = false) MultipartFile[] images,
            @Parameter(description = "썸네일 이미지 인덱스 (기본값: 0)", example = "0")
            @RequestParam(value = "thumbnailIndex", required = false, defaultValue = "0") Integer thumbnailIndex
    ) {
        ProductCreateRequest request = jsonParser.parseAndValidate(productDataJson, ProductCreateRequest.class);
        String sellerId = authenticatedMember.memberId().toString();
        ProductResponse response = productCreateUseCase.createProduct(sellerId, request, images, thumbnailIndex);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 상품 구매 가능 여부 조회 API (Order 모듈에서 사용) 상품의 재고와 상태를 확인하여 구매 가능 여부 반환
     *
     * @param productRequests 검사할 상품 목록 (productId, quantity)
     * @return 상품 구매 가능 상태 목록 (200 OK)
     */
    @PostMapping("/check-availability")
    public ResponseEntity<List<ProductAvailabilityResponse>> deductStock(
            @Valid @RequestBody List<ProductCheckRequest> productRequests
    ) {
        List<ProductAvailabilityResponse> response = productUpdateUseCase.deductStock(productRequests);
        return ResponseEntity.ok(response);
    }

    /**
     * 상품 ID 목록으로 상품 조회 API (장바구니/찜 화면 구성용)
     *
     * @param productIds 조회할 상품 ID 목록
     * @return 상품 목록 (200 OK)
     */
    @GetMapping("/by-ids")
    public ResponseEntity<List<ProductResponse>> findProductsByIds(
            @RequestParam List<UUID> productIds
    ) {
        List<ProductResponse> response = productSearchUseCase.findByProductIds(productIds);
        return ResponseEntity.ok(response);
    }

    /**
     * 상품 통합 검색 API (ACTIVE 상품만, 페이징)
     *
     * @param categoryId 카테고리 ID (선택, 하위 카테고리 포함)
     * @param keyword 검색 키워드 (선택, 상품명/설명 검색)
     * @param minPrice 최소 가격 (선택)
     * @param maxPrice 최대 가격 (선택)
     * @param pageable 페이징 및 정렬 (page, size, sort)
     * @return 상품 목록 (200 OK)
     */
    @GetMapping
    public ResponseEntity<Page<ProductResponse>> findDisplayProducts(
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) java.math.BigDecimal minPrice,
            @RequestParam(required = false) java.math.BigDecimal maxPrice,
            Pageable pageable
    ) {
        Page<ProductResponse> response = productSearchUseCase.findDisplayProducts(
                categoryId,
                keyword,
                minPrice,
                maxPrice,
                pageable
        );
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "인기 상품 조회",
            description = "조회수 기준 인기 상품 목록을 조회합니다 (ACTIVE 상품만). 조회수가 같으면 최신 등록순으로 정렬됩니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공",
                            content = @Content(schema = @Schema(implementation = Page.class)))
            }
    )
    @GetMapping("/popular")
    public ResponseEntity<Page<ProductResponse>> findPopularProducts(Pageable pageable) {
        Page<ProductResponse> response = productSearchUseCase.findPopularProducts(pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * 관리자용 전체 상품 조회 API (모든 상태, 페이징)
     *
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
            @CurrentMember AuthenticatedMember authenticatedMember,
            Pageable pageable
    ) {
        String sellerId = authenticatedMember.memberId().toString();
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
     * @param authenticatedMember 인증된 사용자 정보 (Header: X-Member-Id, X-Member-Role)
     * @param productId           수정할 상품 ID
     * @param request             수정 요청 데이터
     * @return 수정된 상품 정보 (200 OK)
     */
    @PutMapping("/{productId}")
    public ResponseEntity<ProductResponse> updateProduct(
            @CurrentMember AuthenticatedMember authenticatedMember,
            @PathVariable String productId,
            @Valid @RequestBody ProductUpdateRequest request
    ) {
        String sellerId = authenticatedMember.memberId().toString();
        ProductResponse response = productUpdateUseCase.updateProduct(sellerId, productId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 상품 삭제 API (소프트 삭제)
     *
     * @param authenticatedMember 인증된 사용자 정보 (Header: X-Member-Id, X-Member-Role)
     * @param productId           삭제할 상품 ID
     * @return 204 No Content
     */
    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProduct(
            @CurrentMember AuthenticatedMember authenticatedMember,
            @PathVariable String productId
    ) {
        String sellerId = authenticatedMember.memberId().toString();
        productDeleteUseCase.deleteProduct(sellerId, productId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "재고 증가",
            description = "상품의 재고를 증가시킵니다 (입고). 재고가 0이었다면 SOLD_OUT → ACTIVE 상태로 자동 전환됩니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "재고 증가 성공",
                            content = @Content(schema = @Schema(implementation = ProductResponse.class))),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청"),
                    @ApiResponse(responseCode = "403", description = "권한 없음 (판매자 불일치)"),
                    @ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음")
            }
    )
    @PatchMapping("/{productId}/stock/increase")
    public ResponseEntity<ProductResponse> increaseStock(
            @CurrentMember AuthenticatedMember authenticatedMember,
            @PathVariable String productId,
            @Valid @RequestBody StockAdjustmentRequest request
    ) {
        String sellerId = authenticatedMember.memberId().toString();
        ProductResponse response = productUpdateUseCase.increaseStock(sellerId, productId, request.quantity());
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "재고 감소",
            description = "상품의 재고를 감소시킵니다 (출고, 판매 등). 재고가 부족하면 예외가 발생합니다. 재고가 0이 되면 ACTIVE → SOLD_OUT 상태로 자동 전환됩니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "재고 감소 성공",
                            content = @Content(schema = @Schema(implementation = ProductResponse.class))),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청 (재고 부족 등)"),
                    @ApiResponse(responseCode = "403", description = "권한 없음 (판매자 불일치)"),
                    @ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음")
            }
    )
    @PatchMapping("/{productId}/stock/decrease")
    public ResponseEntity<ProductResponse> decreaseStock(
            @CurrentMember AuthenticatedMember authenticatedMember,
            @PathVariable String productId,
            @Valid @RequestBody StockAdjustmentRequest request
    ) {
        String sellerId = authenticatedMember.memberId().toString();
        ProductResponse response = productUpdateUseCase.decreaseStock(sellerId, productId, request.quantity());
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "상품 상태 변경",
            description = "상품의 판매 상태를 변경합니다 (ACTIVE: 판매중, INACTIVE: 판매중지, SOLD_OUT: 품절). 재고가 남아있는 상품은 SOLD_OUT으로 변경할 수 없습니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "상태 변경 성공",
                            content = @Content(schema = @Schema(implementation = ProductResponse.class))),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청 (재고 있는 상품을 품절로 변경 등)"),
                    @ApiResponse(responseCode = "403", description = "권한 없음 (판매자 불일치)"),
                    @ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음")
            }
    )
    @PatchMapping("/{productId}/status")
    public ResponseEntity<ProductResponse> updateStatus(
            @CurrentMember AuthenticatedMember authenticatedMember,
            @PathVariable String productId,
            @Valid @RequestBody ProductStatusUpdateRequest request
    ) {
        String sellerId = authenticatedMember.memberId().toString();
        ProductResponse response = productUpdateUseCase.updateStatus(sellerId, productId, request.status());
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "삭제된 상품 복구",
            description = "소프트 삭제된 상품을 복구합니다. 복구 시 상태는 ACTIVE로 변경됩니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "복구 성공",
                            content = @Content(schema = @Schema(implementation = ProductResponse.class))),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청 (이미 활성 상품 등)"),
                    @ApiResponse(responseCode = "403", description = "권한 없음 (판매자 불일치)"),
                    @ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음")
            }
    )
    @PostMapping("/{productId}/restore")
    public ResponseEntity<ProductResponse> restoreProduct(
            @CurrentMember AuthenticatedMember authenticatedMember,
            @PathVariable String productId
    ) {
        String sellerId = authenticatedMember.memberId().toString();
        ProductResponse response = productUpdateUseCase.restoreProduct(sellerId, productId);
        return ResponseEntity.ok(response);
    }
}
