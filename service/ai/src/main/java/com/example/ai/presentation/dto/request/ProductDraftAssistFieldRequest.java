package com.example.ai.presentation.dto.request;

import com.example.ai.application.dto.ProductDraftAssistField;
import com.example.ai.application.dto.ProductDraftAssistFieldKey;
import com.example.ai.common.exception.ProductDraftAssistInputFieldInvalidException;
import io.swagger.v3.oas.annotations.media.Schema;

public record ProductDraftAssistFieldRequest(
        @Schema(description = "추천을 받을 입력 필드", example = "TITLE", allowableValues = {"TITLE", "DESCRIPTION", "PRICE"})
        ProductDraftAssistFieldKey fieldKey,

        @Schema(description = "화면에 표시되는 필드명", example = "상품명")
        String fieldLabel,

        @Schema(description = "추천 결과 최대 길이", example = "60")
        Integer maxLength,

        @Schema(description = "사용자가 현재 입력한 값", example = "곰돌이 반팔 티셔츠")
        String currentValue
) {

    public ProductDraftAssistField toCommand() {
        validate();
        return new ProductDraftAssistField(
                fieldKey,
                normalize(fieldLabel),
                maxLength,
                normalize(currentValue)
        );
    }

    public void validate() {
        if (fieldKey == null) {
            throw new ProductDraftAssistInputFieldInvalidException();
        }

        if (fieldLabel == null || fieldLabel.isBlank()) {
            throw new ProductDraftAssistInputFieldInvalidException();
        }

        if (maxLength != null && maxLength <= 0) {
            throw new ProductDraftAssistInputFieldInvalidException();
        }
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim();
    }
}
