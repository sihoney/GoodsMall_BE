package com.example.ai.application.service;

import com.example.ai.application.dto.ProductDraftAssistResult;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ProductDraftAssistResultRefiner {

    private static final int DEFAULT_KEYWORD_LIMIT = 5;

    public ProductDraftAssistResult refine(ProductDraftAssistResult result) {
        return new ProductDraftAssistResult(
                normalize(result.suggestedTitle()),
                normalize(result.suggestedDescription()),
                refinePrice(result.suggestedPrice()),
                refineKeywords(result.suggestedKeywords()),
                normalize(result.notes())
        );
    }

    private BigDecimal refinePrice(BigDecimal value) {
        if (value == null || value.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO;
        }
        return value.stripTrailingZeros();
    }

    private List<String> refineKeywords(List<String> keywords) {
        if (keywords == null || keywords.isEmpty()) {
            return List.of();
        }

        return keywords.stream()
                .map(this::normalize)
                .filter(keyword -> !keyword.isBlank())
                .distinct()
                .limit(DEFAULT_KEYWORD_LIMIT)
                .toList();
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim();
    }
}
