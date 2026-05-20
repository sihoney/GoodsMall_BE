package com.example.payment.application.dto;

import java.util.List;

/**
 * 조회 목록 응답에서 공통으로 사용하는 페이지 결과다.
 */
public record PagedResult<T>(
        List<T> items,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext
) {
}
