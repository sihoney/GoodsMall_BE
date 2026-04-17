package com.example.ai.application.service;

import com.example.ai.application.dto.ProductEmbeddingCommand;
import org.springframework.stereotype.Component;

@Component
public class ProductEmbeddingTextComposer {

    public String compose(ProductEmbeddingCommand command) {
        String name = normalize(command.productName());
        String category = normalize(command.categoryName());
        String description = normalize(command.description());
        return String.join("\n", name, category, description);
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return value.trim();
    }
}
