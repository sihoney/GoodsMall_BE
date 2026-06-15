package com.example.member.auth.presentation.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "?좏겙 ?щ컻湲??붿껌")
public record TokenRefreshRequest(
        @Schema(description = "由ы봽?덉떆 ?좏겙")
        // TODO: decide whether refresh token must be validated by @NotBlank or kept optional because cookie fallback is supported.
        String refreshToken
) {
}
