package com.example.ai.application.dto;

import java.math.BigDecimal;
import java.util.List;

public record ProductDraftAssistResult(
        String suggestedTitle,
        String suggestedDescription,
        BigDecimal suggestedPrice,
        List<String> suggestedKeywords,
        String notes
) {
}
