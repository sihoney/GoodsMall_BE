package com.example.ai.application.dto;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public record ProductDraftAssistCommand(
        List<MultipartFile> images,
        List<ProductDraftAssistField> inputFields,
        String titleDraft,
        String descriptionDraft,
        String priceDraft,
        String categoryName,
        String categoryPathText,
        int thumbnailIndex
) {
}
