package com.example.ai.application.service;

import com.example.ai.application.dto.ProductDraftAssistCommand;
import com.example.ai.application.dto.ProductDraftAssistField;
import com.example.ai.common.exception.AiProductDraftAssistException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class ProductDraftAssistFingerprintGenerator {

    public String generate(ProductDraftAssistCommand command) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            updateText(digest, command.titleDraft());
            updateText(digest, command.descriptionDraft());
            updateText(digest, command.priceDraft());
            updateText(digest, command.categoryName());
            updateText(digest, command.categoryPathText());
            updateText(digest, String.valueOf(command.thumbnailIndex()));

            for (ProductDraftAssistField inputField : command.inputFields()) {
                updateText(digest, inputField.fieldKey().name());
                updateText(digest, inputField.fieldLabel());
                updateText(digest, String.valueOf(inputField.maxLength()));
                updateText(digest, inputField.currentValue());
            }

            for (MultipartFile image : command.images()) {
                updateText(digest, image.getOriginalFilename());
                updateText(digest, image.getContentType());
                updateText(digest, String.valueOf(image.getSize()));
                digest.update(image.getBytes());
            }

            return toHex(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new AiProductDraftAssistException("요청 fingerprint 생성 알고리즘을 찾을 수 없습니다.", e);
        } catch (IOException e) {
            throw new AiProductDraftAssistException("요청 fingerprint 생성을 위한 이미지 읽기에 실패했습니다.", e);
        }
    }

    private void updateText(MessageDigest digest, String value) {
        String normalized = value == null ? "" : value.trim();
        digest.update(normalized.getBytes(StandardCharsets.UTF_8));
        digest.update((byte) '\n');
    }

    private String toHex(byte[] bytes) {
        StringBuilder builder = new StringBuilder(bytes.length * 2);
        for (byte value : bytes) {
            builder.append(String.format("%02x", value));
        }
        return builder.toString();
    }
}
