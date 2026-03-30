package com.example.product.presentation.controller;

import com.example.product.application.usecase.ProductImageUploadUseCase;
import com.example.product.presentation.dto.response.ProductImageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 상품 이미지 API Controller
 */
@Tag(name = "Product Image", description = "상품 이미지 관리 API")
@Slf4j
@RestController
@RequestMapping("/api/products/{productId}/images")
@RequiredArgsConstructor
public class ProductImageController {

    private final ProductImageUploadUseCase productImageUploadUseCase;

    @Operation(
            summary = "상품 이미지 추가 업로드",
            description = "기존 상품에 이미지를 추가로 업로드합니다.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "이미지 업로드 성공",
                            content = @Content(schema = @Schema(implementation = ProductImageResponse.class))),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청"),
                    @ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음")
            }
    )
    @PostMapping
    public ResponseEntity<ProductImageResponse> uploadImage(
            @Parameter(description = "상품 ID", required = true) @PathVariable UUID productId,
            @Parameter(description = "업로드할 이미지 파일", required = true) @RequestParam("file") MultipartFile file,
            @Parameter(description = "정렬 순서", example = "0") @RequestParam(value = "sortOrder", defaultValue = "0") Integer sortOrder,
            @Parameter(description = "썸네일 여부", example = "false") @RequestParam(value = "isThumbnail", defaultValue = "false") Boolean isThumbnail
    ) {
        log.info("Upload image request: productId={}, sortOrder={}, isThumbnail={}",
                productId, sortOrder, isThumbnail);

        ProductImageResponse response = productImageUploadUseCase.uploadImage(
                productId,
                file,
                sortOrder,
                isThumbnail
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
