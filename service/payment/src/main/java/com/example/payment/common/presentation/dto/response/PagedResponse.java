package com.example.payment.common.presentation.dto.response;

import java.util.List;

/**
 * ?꾨줎??紐⑸줉 ?묐떟?먯꽌 怨듯넻?쇰줈 ?ъ슜?섎뒗 ?섏씠吏 DTO??
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
