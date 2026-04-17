package com.example.ai.application.service;

import com.example.ai.application.dto.ProductDraftAssistCommand;
import com.example.ai.application.usecase.ProductDraftAssistUseCase;
import com.example.ai.domain.service.ProductDraftGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@lombok.RequiredArgsConstructor
public class ProductDraftAssistService implements ProductDraftAssistUseCase {

    private final ProductDraftGenerator productDraftGenerator;

    @Override
    public com.example.ai.application.dto.ProductDraftAssistResult createProductDraft(ProductDraftAssistCommand command) {
        log.info(
                "Product draft assist requested. imageCount={}, inputFieldCount={}, categoryName={}, categoryPathText={}, thumbnailIndex={}",
                command.images().size(),
                command.inputFields().size(),
                command.categoryName(),
                command.categoryPathText(),
                command.thumbnailIndex()
        );
        return productDraftGenerator.generate(command);
    }
}
