package com.example.ai.application.service;

import com.example.ai.application.dto.ProductDraftAssistCommand;
import com.example.ai.application.dto.ProductDraftAssistField;
import com.example.ai.application.dto.ProductDraftAssistFieldKey;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ProductDraftAssistPromptBuilder {

    public String buildSystemPrompt() {
        return """
                너는 중고 상품 등록 보조 AI다.
                입력된 이미지와 초안 정보를 바탕으로 상품 등록 초안을 추천한다.
                응답은 반드시 JSON 객체 하나만 반환한다.
                응답 형식:
                {
                  "suggestedTitle": "string",
                  "suggestedDescription": "string",
                  "suggestedPrice": 0,
                  "suggestedKeywords": ["string"],
                  "notes": "string"
                }
                규칙:
                - 이미지에 없는 정보는 단정하지 않는다.
                - 확실하지 않은 내용은 notes에 분리한다.
                - suggestedPrice는 확정 가격이 아니라 추천 가격이다.
                - 숫자가 확실하지 않으면 현재 입력 가격을 참고하되 과도한 추정은 피한다.
                - 응답 본문에 JSON 외 텍스트를 추가하지 않는다.
                """;
    }

    public String buildUserPrompt(ProductDraftAssistCommand command) {
        StringBuilder builder = new StringBuilder();
        builder.append("현재 상품 등록 초안 정보\n");
        builder.append("- categoryName: ").append(normalize(command.categoryName())).append('\n');
        builder.append("- categoryPathText: ").append(normalize(command.categoryPathText())).append('\n');
        builder.append("- titleDraft: ").append(normalize(command.titleDraft())).append('\n');
        builder.append("- descriptionDraft: ").append(normalize(command.descriptionDraft())).append('\n');
        builder.append("- priceDraft: ").append(normalize(command.priceDraft())).append('\n');
        builder.append("- thumbnailIndex: ").append(command.thumbnailIndex()).append('\n');
        builder.append("- imageCount: ").append(command.images().size()).append('\n');
        builder.append('\n');
        builder.append("입력 칸 정보\n");
        for (ProductDraftAssistField inputField : command.inputFields()) {
            builder.append("- ")
                    .append(inputField.fieldKey().name())
                    .append(" (label=")
                    .append(inputField.fieldLabel())
                    .append(", maxLength=")
                    .append(inputField.maxLength())
                    .append(", currentValue=")
                    .append(normalize(inputField.currentValue()))
                    .append(")\n");
        }
        builder.append('\n');
        builder.append("응답 작성 지침\n");
        builder.append("- TITLE 필드가 있으면 상품명을 추천한다.\n");
        builder.append("- DESCRIPTION 필드가 있으면 상품 설명을 추천한다.\n");
        builder.append("- PRICE 필드가 있으면 suggestedPrice를 채운다.\n");
        builder.append("- suggestedKeywords는 검색/요약 참고용 핵심 키워드 3~5개 이내로 작성한다.\n");
        builder.append("- notes에는 판매자가 추가 확인해야 할 내용을 짧게 정리한다.\n");
        builder.append("- 가격은 숫자만 반환하고 통화 기호나 쉼표를 넣지 않는다.\n");
        builder.append("- PRICE 필드가 없으면 suggestedPrice는 0으로 반환한다.\n");
        builder.append("- TITLE, DESCRIPTION, PRICE 외 필드는 무시한다.\n");
        builder.append('\n');
        builder.append("요청된 입력 필드: ").append(buildRequestedFieldSummary(command.inputFields()));
        return builder.toString();
    }

    private String buildRequestedFieldSummary(List<ProductDraftAssistField> inputFields) {
        return inputFields.stream()
                .map(ProductDraftAssistField::fieldKey)
                .map(ProductDraftAssistFieldKey::name)
                .distinct()
                .reduce((left, right) -> left + ", " + right)
                .orElse("없음");
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return "(empty)";
        }
        return value.trim();
    }
}
