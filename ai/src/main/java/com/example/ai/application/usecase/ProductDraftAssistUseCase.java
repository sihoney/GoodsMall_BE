package com.example.ai.application.usecase;

import com.example.ai.application.dto.ProductDraftAssistCommand;
import com.example.ai.application.dto.ProductDraftAssistResult;

public interface ProductDraftAssistUseCase {

    ProductDraftAssistResult createProductDraft(ProductDraftAssistCommand command);
}
