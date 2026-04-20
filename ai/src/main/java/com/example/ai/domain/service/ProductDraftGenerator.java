package com.example.ai.domain.service;

import com.example.ai.application.dto.ProductDraftAssistCommand;
import com.example.ai.application.dto.ProductDraftAssistResult;

public interface ProductDraftGenerator {

    ProductDraftAssistResult generate(ProductDraftAssistCommand command);
}
