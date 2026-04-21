package com.example.ai.presentation.controller;

import com.example.ai.application.usecase.ProductDraftAssistUseCase;
import com.example.ai.common.exception.ProductDraftAssistInputFieldInvalidException;
import com.example.ai.presentation.dto.request.ProductDraftAssistRequest;
import com.example.ai.presentation.dto.response.ApiResponse;
import com.example.ai.presentation.dto.response.ProductDraftAssistResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
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
    private final ObjectMapper objectMapper;

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
                    OpenAI 호출 실패, 응답 파싱 실패, 중복 요청 대기 초과 시에는 fallback 초안을 반환할 수 있습니다.
                    설정 누락이나 잘못된 입력 검증 실패는 에러 응답으로 반환됩니다.
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
                                        "suggestedPrice": 39000,
                                        "suggestedKeywords": [],
                                        "notes": "브랜드와 상품 상태는 판매자가 다시 확인해 주세요."
                                      },
                                      "error": null
                                    }
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": false,
                                      "data": null,
                                      "error": {
                                        "code": "AI_ASSIST_IMAGE_REQUIRED",
                                        "message": "이미지는 최소 1개 이상 필요합니다."
                                      }
                                    }
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "설정 또는 내부 처리 오류",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": false,
                                      "data": null,
                                      "error": {
                                        "code": "AI_PRODUCT_DRAFT_ASSIST_CONFIGURATION_ERROR",
                                        "message": "ai.product-draft.assist.openai-api-key 설정이 필요합니다."
                                      }
                                    }
                                    """)
                    )
            )
    })
    public ResponseEntity<ApiResponse<ProductDraftAssistResponse>> createProductDraft(
            @Parameter(
                    description = "상품 이미지 파일 목록. Swagger에서 JPG, PNG, WEBP, GIF 중 1개 이상 업로드합니다."
            )
            @RequestPart("images") List<MultipartFile> images,

            @Parameter(
                    description = "상품 초안 추천 요청 JSON",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProductDraftAssistRequest.class),
                            examples = @ExampleObject(name = "상품 초안 추천 요청", value = """
                                    {
                                      "inputFields": [
                                        {
                                          "fieldKey": "TITLE",
                                          "fieldLabel": "상품명",
                                          "maxLength": 60,
                                          "currentValue": "곰돌이 반팔 티셔츠"
                                        },
                                        {
                                          "fieldKey": "DESCRIPTION",
                                          "fieldLabel": "상품 설명",
                                          "maxLength": 1000,
                                          "currentValue": "화이트 색상의 귀여운 캐릭터 반팔 티셔츠입니다."
                                        },
                                        {
                                          "fieldKey": "PRICE",
                                          "fieldLabel": "판매가",
                                          "maxLength": 10,
                                          "currentValue": "25000"
                                        }
                                      ],
                                      "titleDraft": "곰돌이 반팔 티셔츠",
                                      "descriptionDraft": "화이트 색상의 귀여운 캐릭터 반팔 티셔츠입니다.",
                                      "priceDraft": "25000",
                                      "categoryName": "의류",
                                      "categoryPathText": "패션 > 의류 > 상의",
                                      "thumbnailIndex": 0
                                    }
                                    """)
                    )
            )
            @RequestPart("request") String requestJson
    ) {
        ProductDraftAssistRequest request = parseRequest(requestJson);
        ProductDraftAssistResponse response = ProductDraftAssistResponse.from(
                productDraftAssistUseCase.createProductDraft(request.toCommand(images))
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    private ProductDraftAssistRequest parseRequest(String requestJson) {
        try {
            return objectMapper.readValue(requestJson, ProductDraftAssistRequest.class);
        } catch (JsonProcessingException e) {
            throw new ProductDraftAssistInputFieldInvalidException();
        }
    }
}
