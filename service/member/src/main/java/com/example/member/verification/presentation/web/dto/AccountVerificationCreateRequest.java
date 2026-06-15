package com.example.member.verification.presentation.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "怨꾩쥖 ?몄쬆 ?앹꽦 ?붿껌")
public record AccountVerificationCreateRequest(
        @NotBlank(message = "??됰챸? ?꾩닔?낅땲??")
        @Schema(description = "??됰챸", example = "KAKAO")
        String bankName,
        @NotBlank(message = "怨꾩쥖踰덊샇???꾩닔?낅땲??")
        @Schema(description = "怨꾩쥖踰덊샇", example = "1234567890123")
        // TODO: add @Pattern for numeric account numbers when the accepted format is finalized.
        String accountNumber
) {
}
