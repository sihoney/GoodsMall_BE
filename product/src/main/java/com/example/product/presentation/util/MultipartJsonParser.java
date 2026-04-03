package com.example.product.presentation.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Multipart 요청의 JSON 파트를 파싱하고 검증하는 유틸리티
 * Swagger UI 호환성을 위해 String으로 받은 JSON을 객체로 변환
 */
@Component
@RequiredArgsConstructor
public class MultipartJsonParser {

    private final ObjectMapper objectMapper;
    private final Validator validator;

    /**
     * JSON 문자열을 객체로 변환하고 검증
     *
     * @param json JSON 문자열
     * @param clazz 변환할 클래스 타입
     * @return 변환된 객체
     * @throws IllegalArgumentException JSON 파싱 실패 또는 검증 실패 시
     */
    public <T> T parseAndValidate(String json, Class<T> clazz) {
        try {
            T object = objectMapper.readValue(json, clazz);

            Set<ConstraintViolation<T>> violations = validator.validate(object);
            if (!violations.isEmpty()) {
                String errorMessage = violations.stream()
                        .map(ConstraintViolation::getMessage)
                        .collect(Collectors.joining(", "));
                throw new IllegalArgumentException("Validation failed: " + errorMessage);
            }

            return object;

        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid JSON data: " + e.getMessage(), e);
        }
    }
}
