package com.example.ai.presentation.dto.response;

import com.example.ai.application.dto.ProductDraftAssistResult;
import java.util.List;

public record ProductDraftAssistResponse(
        String suggestedTitle,
        String suggestedDescription,
        List<String> suggestedKeywords,
        String notes
) {

    public static ProductDraftAssistResponse from(ProductDraftAssistResult result) {
        return new ProductDraftAssistResponse(
                result.suggestedTitle(),
                result.suggestedDescription(),
                result.suggestedKeywords(),
                result.notes()
        );
    }
}
