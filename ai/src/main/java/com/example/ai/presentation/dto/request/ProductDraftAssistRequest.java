package com.example.ai.presentation.dto.request;

import com.example.ai.application.dto.ProductDraftAssistCommand;
import com.example.ai.application.dto.ProductDraftAssistField;
import com.example.ai.common.exception.ProductDraftAssistDuplicateFieldKeyException;
import com.example.ai.common.exception.ProductDraftAssistImageCountExceededException;
import com.example.ai.common.exception.ProductDraftAssistImageEmptyException;
import com.example.ai.common.exception.ProductDraftAssistImageRequiredException;
import com.example.ai.common.exception.ProductDraftAssistImageTooLargeException;
import com.example.ai.common.exception.ProductDraftAssistInputFieldsRequiredException;
import com.example.ai.common.exception.ProductDraftAssistThumbnailIndexInvalidException;
import com.example.ai.common.exception.ProductDraftAssistUnsupportedImageTypeException;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.web.multipart.MultipartFile;

public record ProductDraftAssistRequest(
        List<ProductDraftAssistFieldRequest> inputFields,
        String titleDraft,
        String descriptionDraft,
        String priceDraft,
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
                normalize(priceDraft),
                normalize(categoryName),
                normalize(categoryPathText),
                resolveThumbnailIndex()
        );
    }

    private void validateImages(List<MultipartFile> images) {
        if (images == null || images.isEmpty()) {
            throw new ProductDraftAssistImageRequiredException();
        }

        if (images.size() > MAX_IMAGE_COUNT) {
            throw new ProductDraftAssistImageCountExceededException();
        }

        for (MultipartFile image : images) {
            if (image == null || image.isEmpty()) {
                throw new ProductDraftAssistImageEmptyException();
            }

            if (image.getSize() > MAX_IMAGE_SIZE) {
                throw new ProductDraftAssistImageTooLargeException();
            }

            String contentType = image.getContentType();
            if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType)) {
                throw new ProductDraftAssistUnsupportedImageTypeException();
            }
        }
    }

    private void validateInputFields() {
        if (inputFields == null || inputFields.isEmpty()) {
            throw new ProductDraftAssistInputFieldsRequiredException();
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
            throw new ProductDraftAssistDuplicateFieldKeyException();
        }
    }

    private void validateThumbnailIndex(List<MultipartFile> images) {
        if (thumbnailIndex == null) {
            return;
        }

        if (thumbnailIndex < 0) {
            throw new ProductDraftAssistThumbnailIndexInvalidException();
        }

        if (thumbnailIndex >= images.size()) {
            throw new ProductDraftAssistThumbnailIndexInvalidException();
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
