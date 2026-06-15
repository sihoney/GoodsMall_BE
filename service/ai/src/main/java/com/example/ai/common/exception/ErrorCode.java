package com.example.ai.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "입력값이 올바르지 않습니다."),
    AI_ASSIST_IMAGE_REQUIRED(HttpStatus.BAD_REQUEST, "이미지는 최소 1개 이상 필요합니다."),
    AI_ASSIST_IMAGE_COUNT_EXCEEDED(HttpStatus.BAD_REQUEST, "이미지는 최대 5개까지 업로드할 수 있습니다."),
    AI_ASSIST_IMAGE_EMPTY(HttpStatus.BAD_REQUEST, "비어 있는 이미지 파일은 업로드할 수 없습니다."),
    AI_ASSIST_IMAGE_TOO_LARGE(HttpStatus.BAD_REQUEST, "이미지 파일은 각각 5MB 이하여야 합니다."),
    AI_ASSIST_IMAGE_REQUEST_TOO_LARGE(HttpStatus.BAD_REQUEST, "이미지 업로드 요청 전체 크기는 30MB 이하여야 합니다."),
    AI_ASSIST_UNSUPPORTED_IMAGE_TYPE(HttpStatus.BAD_REQUEST, "JPG, PNG, WEBP, GIF 형식의 이미지만 업로드할 수 있습니다."),
    AI_ASSIST_INPUT_FIELDS_REQUIRED(HttpStatus.BAD_REQUEST, "inputFields는 최소 1개 이상 필요합니다."),
    AI_ASSIST_INPUT_FIELD_INVALID(HttpStatus.BAD_REQUEST, "inputFields 항목이 올바르지 않습니다."),
    AI_ASSIST_DUPLICATE_FIELD_KEY(HttpStatus.BAD_REQUEST, "inputFields에 중복된 fieldKey가 포함되어 있습니다."),
    AI_ASSIST_THUMBNAIL_INDEX_INVALID(HttpStatus.BAD_REQUEST, "thumbnailIndex 값이 올바르지 않습니다."),

    AI_EMBEDDING_ERROR(HttpStatus.UNPROCESSABLE_CONTENT, "AI 임베딩 처리 중 오류가 발생했습니다."),
    AI_PRODUCT_DRAFT_ASSIST_ERROR(HttpStatus.UNPROCESSABLE_CONTENT, "상품 등록 보조 AI 처리 중 오류가 발생했습니다."),
    AI_PRODUCT_DRAFT_ASSIST_CONFIGURATION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "상품 등록 보조 AI 설정 오류가 발생했습니다."),
    AI_PRODUCT_DRAFT_ASSIST_EXTERNAL_CALL_ERROR(HttpStatus.BAD_GATEWAY, "상품 등록 보조 AI 외부 호출 중 오류가 발생했습니다."),
    AI_PRODUCT_DRAFT_ASSIST_RESPONSE_INVALID_ERROR(HttpStatus.BAD_GATEWAY, "상품 등록 보조 AI 응답 형식이 올바르지 않습니다."),
    AI_PRODUCT_DRAFT_ASSIST_IMAGE_READ_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "상품 등록 보조 AI 이미지 처리 중 오류가 발생했습니다."),
    AI_PRODUCT_DRAFT_ASSIST_FINGERPRINT_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "상품 등록 보조 AI 요청 식별값 생성 중 오류가 발생했습니다."),

    AI_AUCTION_PRICE_RECOMMENDATION_ERROR(HttpStatus.UNPROCESSABLE_CONTENT, "경매 가격 추천 AI 처리 중 오류가 발생했습니다."),
    AI_AUCTION_PRICE_RECOMMENDATION_REQUEST_INVALID(HttpStatus.BAD_REQUEST, "경매 가격 추천 요청 값이 올바르지 않습니다."),
    AI_AUCTION_PRICE_RECOMMENDATION_CONFIGURATION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "경매 가격 추천 AI 설정 오류가 발생했습니다."),
    AI_AUCTION_PRICE_RECOMMENDATION_EXTERNAL_CALL_ERROR(HttpStatus.BAD_GATEWAY, "경매 가격 추천 AI 외부 호출 중 오류가 발생했습니다."),
    AI_AUCTION_PRICE_RECOMMENDATION_RESPONSE_INVALID_ERROR(HttpStatus.BAD_GATEWAY, "경매 가격 추천 AI 응답 형식이 올바르지 않습니다."),
    AI_AUCTION_PRICE_RECOMMENDATION_PROCESSING_ERROR(HttpStatus.UNPROCESSABLE_CONTENT, "경매 가격 추천 계산 처리 중 오류가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
