package com.example.member.presentation.dto;

import java.util.List;

public record MemberOauthAccountListResponse(
        List<MemberOauthAccountResponse> accounts,
        boolean hasPasswordLogin,
        boolean canRemoveLastOauthAccount
) {
}
