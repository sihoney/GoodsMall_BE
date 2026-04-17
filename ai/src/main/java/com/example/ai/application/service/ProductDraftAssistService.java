package com.example.ai.application.service;

import com.example.ai.application.dto.ProductDraftAssistCommand;
import com.example.ai.application.dto.ProductDraftAssistResult;
import com.example.ai.application.usecase.ProductDraftAssistUseCase;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ProductDraftAssistService implements ProductDraftAssistUseCase {

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

        String suggestedTitle = normalizeText(command.titleDraft());
        String suggestedDescription = normalizeText(command.descriptionDraft());

        return new ProductDraftAssistResult(
                suggestedTitle,
                suggestedDescription,
                List.of(),
                "상품 등록 보조 AI 기본 API 뼈대가 준비되었습니다. 상세 추천 로직은 다음 커밋에서 확장됩니다."
        );
    }

    private String normalizeText(String value) {
        if (value == null) {
            return "";
        }
        return value.trim();
    }
}
