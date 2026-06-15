package com.example.payment.common.application.dto;

import java.util.List;

/**
 * 議고쉶 紐⑸줉 ?묐떟?먯꽌 怨듯넻?쇰줈 ?ъ슜?섎뒗 ?섏씠吏 寃곌낵??
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
