package com.example.ai.presentation.controller;

import com.example.ai.application.usecase.ProductDraftAssistUseCase;
import com.example.ai.presentation.dto.request.ProductDraftAssistRequest;
import com.example.ai.presentation.dto.response.ApiResponse;
import com.example.ai.presentation.dto.response.ProductDraftAssistResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ai/assist")
@Tag(name = "AI Product Draft Assist", description = "이미지 기반 상품 등록 보조 API")
public class ProductDraftAssistController {

    private final ProductDraftAssistUseCase productDraftAssistUseCase;

    @PostMapping(
            value = "/product-draft-from-image",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @Operation(
            summary = "이미지 기반 상품 등록 보조",
            description = """
                    이미지와 초안 생성 요청 정보를 받아 상품명/설명 추천 초안을 생성합니다.
                    multipart 요청 파트는 다음 구조를 사용합니다.
                    - images: 업로드할 이미지 파일 목록
                    - request: JSON 요청 객체
                    request.inputFields.fieldKey 허용값은 TITLE, DESCRIPTION, PRICE 입니다.
                    thumbnailIndex가 없으면 첫 번째 이미지를 대표 이미지로 사용합니다.
                    """
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "추천 생성 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": true,
                                      "data": {
                                        "suggestedTitle": "AI 추천 상품명",
                                        "suggestedDescription": "AI 추천 상품 설명",
                                        "suggestedKeywords": [],
                                        "notes": "상품 등록 보조 AI 기본 API 뼈대가 준비되었습니다."
                                      },
                                      "error": null
                                    }
                                    """)
                    )
            )
    })
    public ResponseEntity<ApiResponse<ProductDraftAssistResponse>> createProductDraft(
            @RequestPart("images") List<MultipartFile> images,
            @RequestPart("request") ProductDraftAssistRequest request
    ) {
        ProductDraftAssistResponse response = ProductDraftAssistResponse.from(
                productDraftAssistUseCase.createProductDraft(request.toCommand(images))
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
