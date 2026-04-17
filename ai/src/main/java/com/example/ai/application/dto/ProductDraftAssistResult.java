package com.example.ai.application.dto;

import java.util.List;

public record ProductDraftAssistResult(
        String suggestedTitle,
        String suggestedDescription,
        List<String> suggestedKeywords,
        String notes
) {
}
