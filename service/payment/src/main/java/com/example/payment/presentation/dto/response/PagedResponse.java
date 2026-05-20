package com.example.payment.presentation.dto.response;

import java.util.List;

/**
 * 프론트 목록 응답에서 공통으로 사용하는 페이지 DTO다.
 */
public record PagedResponse<T>(
        List<T> items,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext
) {
}
