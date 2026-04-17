package com.example.ai.presentation.dto.request;

import com.example.ai.application.dto.ProductDraftAssistField;
import com.example.ai.application.dto.ProductDraftAssistFieldKey;
import com.example.ai.common.exception.ProductDraftAssistInputFieldInvalidException;

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
