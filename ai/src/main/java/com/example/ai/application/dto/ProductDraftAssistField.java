package com.example.ai.application.dto;

public record ProductDraftAssistField(
        ProductDraftAssistFieldKey fieldKey,
        String fieldLabel,
        Integer maxLength,
        String currentValue
) {
}
