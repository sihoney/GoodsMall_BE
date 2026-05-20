package com.example.ai.application.usecase;

import com.example.ai.application.dto.ProductDeactivateCommand;
import com.example.ai.application.dto.ProductEmbeddingCommand;

public interface ProductEmbeddingUseCase {

    void embedding(ProductEmbeddingCommand command);

    void deactivate(ProductDeactivateCommand command);
}
