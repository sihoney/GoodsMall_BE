package com.example.ai.application.service;

import com.example.ai.application.dto.ProductDraftAssistCommand;
import com.example.ai.application.dto.ProductDraftAssistResult;
import com.example.ai.common.exception.AiProductDraftAssistException;
import com.example.ai.application.usecase.ProductDraftAssistUseCase;
import com.example.ai.domain.service.ProductDraftGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@lombok.RequiredArgsConstructor
public class ProductDraftAssistService implements ProductDraftAssistUseCase {

    private final ProductDraftGenerator productDraftGenerator;
    private final ProductDraftAssistFallbackFactory fallbackFactory;
    private final ProductDraftAssistResultRefiner resultRefiner;

    @Override
    public ProductDraftAssistResult createProductDraft(ProductDraftAssistCommand command) {
        log.info(
                "Product draft assist requested. imageCount={}, inputFieldCount={}, categoryName={}, categoryPathText={}, thumbnailIndex={}",
                command.images().size(),
                command.inputFields().size(),
                command.categoryName(),
                command.categoryPathText(),
                command.thumbnailIndex()
        );

        ProductDraftAssistResult fallbackResult = fallbackFactory.create(
                command,
                "AI 추천이 불안정할 수 있어 현재 입력값 기준 초안을 우선 반환했습니다."
        );

        try {
            ProductDraftAssistResult generatedResult = productDraftGenerator.generate(command);
            ProductDraftAssistResult mergedResult = fallbackFactory.merge(generatedResult, fallbackResult);
            return resultRefiner.refine(mergedResult);
        } catch (AiProductDraftAssistException e) {
            log.warn("Product draft assist fallback applied. reason={}", e.getMessage(), e);
            return resultRefiner.refine(fallbackResult);
        }
    }
}
