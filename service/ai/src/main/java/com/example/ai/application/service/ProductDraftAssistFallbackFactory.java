package com.example.ai.application.service;

import com.example.ai.application.dto.ProductDraftAssistCommand;
import com.example.ai.application.dto.ProductDraftAssistField;
import com.example.ai.application.dto.ProductDraftAssistFieldKey;
import com.example.ai.application.dto.ProductDraftAssistResult;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ProductDraftAssistFallbackFactory {

    public ProductDraftAssistResult create(ProductDraftAssistCommand command, String notes) {
        return new ProductDraftAssistResult(
                resolveTextValue(command, ProductDraftAssistFieldKey.TITLE, command.titleDraft()),
                resolveTextValue(command, ProductDraftAssistFieldKey.DESCRIPTION, command.descriptionDraft()),
                resolvePriceValue(command),
                List.of(),
                notes
        );
    }

    public ProductDraftAssistResult merge(ProductDraftAssistResult primary, ProductDraftAssistResult fallback) {
        return new ProductDraftAssistResult(
                isBlank(primary.suggestedTitle()) ? fallback.suggestedTitle() : primary.suggestedTitle(),
                isBlank(primary.suggestedDescription()) ? fallback.suggestedDescription() : primary.suggestedDescription(),
                primary.suggestedPrice() == null || primary.suggestedPrice().compareTo(BigDecimal.ZERO) <= 0
                        ? fallback.suggestedPrice()
                        : primary.suggestedPrice(),
                primary.suggestedKeywords() == null ? List.of() : primary.suggestedKeywords(),
                isBlank(primary.notes()) ? fallback.notes() : primary.notes()
        );
    }

    private String resolveTextValue(
            ProductDraftAssistCommand command,
            ProductDraftAssistFieldKey fieldKey,
            String draftValue
    ) {
        if (!isBlank(draftValue)) {
            return draftValue.trim();
        }

        return command.inputFields().stream()
                .filter(field -> field.fieldKey() == fieldKey)
                .map(ProductDraftAssistField::currentValue)
                .filter(value -> !isBlank(value))
                .map(String::trim)
                .findFirst()
                .orElse("");
    }

    private BigDecimal resolvePriceValue(ProductDraftAssistCommand command) {
        if (!isBlank(command.priceDraft())) {
            return parsePrice(command.priceDraft());
        }

        return command.inputFields().stream()
                .filter(field -> field.fieldKey() == ProductDraftAssistFieldKey.PRICE)
                .map(ProductDraftAssistField::currentValue)
                .filter(value -> !isBlank(value))
                .map(this::parsePrice)
                .findFirst()
                .orElse(BigDecimal.ZERO);
    }

    private BigDecimal parsePrice(String value) {
        String normalized = value == null ? "" : value.replace(",", "").trim();
        if (normalized.isEmpty()) {
            return BigDecimal.ZERO;
        }

        try {
            return new BigDecimal(normalized);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
