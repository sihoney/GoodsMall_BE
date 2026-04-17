package com.example.ai.presentation.dto.request;

import com.example.ai.application.dto.ProductDraftAssistCommand;
import com.example.ai.application.dto.ProductDraftAssistField;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.web.multipart.MultipartFile;

public record ProductDraftAssistRequest(
        List<ProductDraftAssistFieldRequest> inputFields,
        String titleDraft,
        String descriptionDraft,
        String categoryName,
        String categoryPathText,
        Integer thumbnailIndex
) {

    private static final int MAX_IMAGE_COUNT = 5;
    private static final long MAX_IMAGE_SIZE = 5L * 1024 * 1024;
    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "image/gif"
    );

    public void validate(List<MultipartFile> images) {
        validateImages(images);
        validateInputFields();
        validateThumbnailIndex(images);
    }

    public ProductDraftAssistCommand toCommand(List<MultipartFile> images) {
        validate(images);

        List<ProductDraftAssistField> draftFields = inputFields.stream()
                .map(ProductDraftAssistFieldRequest::toCommand)
                .toList();

        return new ProductDraftAssistCommand(
                images,
                draftFields,
                normalize(titleDraft),
                normalize(descriptionDraft),
                normalize(categoryName),
                normalize(categoryPathText),
                resolveThumbnailIndex()
        );
    }

    private void validateImages(List<MultipartFile> images) {
        if (images == null || images.isEmpty()) {
            throw new IllegalArgumentException("이미지는 최소 1개 이상 필요합니다.");
        }

        if (images.size() > MAX_IMAGE_COUNT) {
            throw new IllegalArgumentException("이미지는 최대 5개까지 업로드할 수 있습니다.");
        }

        for (MultipartFile image : images) {
            if (image == null || image.isEmpty()) {
                throw new IllegalArgumentException("비어 있는 이미지 파일은 업로드할 수 없습니다.");
            }

            if (image.getSize() > MAX_IMAGE_SIZE) {
                throw new IllegalArgumentException("이미지 파일은 각각 5MB 이하여야 합니다.");
            }

            String contentType = image.getContentType();
            if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType)) {
                throw new IllegalArgumentException("JPG, PNG, WEBP, GIF 형식의 이미지만 업로드할 수 있습니다.");
            }
        }
    }

    private void validateInputFields() {
        if (inputFields == null || inputFields.isEmpty()) {
            throw new IllegalArgumentException("inputFields는 최소 1개 이상 필요합니다.");
        }

        Set<com.example.ai.application.dto.ProductDraftAssistFieldKey> duplicatedFieldKeys = new HashSet<>();
        Set<com.example.ai.application.dto.ProductDraftAssistFieldKey> seenFieldKeys = EnumSet.noneOf(
                com.example.ai.application.dto.ProductDraftAssistFieldKey.class
        );

        for (ProductDraftAssistFieldRequest inputField : inputFields) {
            inputField.validate();
            if (!seenFieldKeys.add(inputField.fieldKey())) {
                duplicatedFieldKeys.add(inputField.fieldKey());
            }
        }

        if (!duplicatedFieldKeys.isEmpty()) {
            throw new IllegalArgumentException("inputFields에 중복된 fieldKey가 포함되어 있습니다.");
        }
    }

    private void validateThumbnailIndex(List<MultipartFile> images) {
        if (thumbnailIndex == null) {
            return;
        }

        if (thumbnailIndex < 0) {
            throw new IllegalArgumentException("thumbnailIndex는 0 이상이어야 합니다.");
        }

        if (thumbnailIndex >= images.size()) {
            throw new IllegalArgumentException("thumbnailIndex는 이미지 개수보다 작아야 합니다.");
        }
    }

    private int resolveThumbnailIndex() {
        if (thumbnailIndex == null) {
            return 0;
        }
        return thumbnailIndex;
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim();
    }
}
