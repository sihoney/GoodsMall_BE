package com.example.ai.presentation.dto.request;

import com.example.ai.application.dto.ProductDraftAssistField;
import com.example.ai.application.dto.ProductDraftAssistFieldKey;

public record ProductDraftAssistFieldRequest(
        ProductDraftAssistFieldKey fieldKey,
        String fieldLabel,
        Integer maxLength,
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
            throw new IllegalArgumentException("inputFields.fieldKey는 필수입니다.");
        }

        if (fieldLabel == null || fieldLabel.isBlank()) {
            throw new IllegalArgumentException("inputFields.fieldLabel은 필수입니다.");
        }

        if (maxLength != null && maxLength <= 0) {
            throw new IllegalArgumentException("inputFields.maxLength는 1 이상이어야 합니다.");
        }
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim();
    }
}
